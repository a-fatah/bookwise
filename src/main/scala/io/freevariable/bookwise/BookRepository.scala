package io.freevariable.bookwise

import cats.effect.IO

import scala.concurrent.ExecutionContext

trait BookRepository {
  def all(): IO[Seq[Book]]
  def get(id: BookId): IO[Option[Book]]
  def getByTitle(title: String): IO[Option[Book]]
  def save(book: Book): IO[BookId]
}

class BookRepositoryImpl extends BookRepository {
  self: BooksSchema with DatabaseProvider =>

  import profile.api._

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  override def get(id: BookId): IO[Option[Book]] = findBook(_.id === id.value)

  override def getByTitle(title: String): IO[Option[Book]] = findBook(_.title === title)

  private def findBook(predicate: BooksTable ⇒ Rep[Boolean]): IO[Option[Book]] = {
    val query = for {
      book <- books.filter(predicate)
      author <- book.author
      publisher <- book.publisher
    } yield (book, author, publisher)

    // run the query and map the result to a Book
    val bookQuery = query.result.headOption.map {
      case Some((book, author, publisher)) => Some(Book(book.title, book.isbn, Author(author.name), Publisher(publisher.name), book.pages))
      case None => None
    }

    IO.fromFuture(IO(db.run(bookQuery)))
  }

  override def all(): IO[Seq[Book]] = {
    val query = for {
      book <- books
      author <- book.author
      publisher <- book.publisher
    } yield (book, author, publisher)

    // run the query and map the result to a Book
    val bookQuery = query.result.flatMap { books =>
      DBIO.successful(books.map {
        case (book, author, publisher) => Book(book.title, book.isbn, Author(author.name), Publisher(publisher.name), book.pages)
      })
    }

    IO.fromFuture(IO(db.run(bookQuery)))
  }

  override def save(book: Book): IO[BookId] = {
    val bookEntity = BookEntity(id = None, title = book.title.value, isbn = book.isbn.value, authorId = None, publisherId = None, pages = book.pages)
    val authorEntity = AuthorEntity(id = None, name = book.author.name)
    val publisherEntity = PublisherEntity(id = None, name = book.publisher.name)

    val authorInsert = authors returning authors.map(_.id) into ((author, id) => author.copy(id = Some(id))) += authorEntity
    val publisherInsert = publishers returning publishers.map(_.id) into ((publisher, id) => publisher.copy(id = Some(id))) += publisherEntity
    val bookInsert = books returning books.map(_.id) into ((book, id) => book.copy(id = Some(id)))

    val inserts = for {
      author <- authorInsert
      publisher <- publisherInsert
      bookWithIds = bookEntity.copy(authorId = author.id, publisherId = publisher.id)
      book <- bookInsert += bookWithIds
      id <- DBIO.successful(book.id.get)
    } yield BookId(id)

    IO.fromFuture(IO(db.run(inserts.transactionally)))
  }

}
