package io.freevariable.bookwise

import cats.effect.IO

import scala.concurrent.ExecutionContext

trait BookService {
  def getAll: IO[Seq[Book]]
  def get(id: BookId): IO[Option[Book]]
  def create(book: Book)(implicit ec: ExecutionContext): IO[(BookId, Book)]
  def getByTitle(title: String): IO[Option[Book]]
}


class BookServiceImpl(private val repository: BookRepository) extends BookService {

  override def getAll: IO[Seq[Book]] = repository.all().map(_.map(mapToBook))

  override def get(id: BookId): IO[Option[Book]] = repository.get(id.value).map(_.map(mapToBook))

  override def create(book: Book)(implicit ec: ExecutionContext): IO[(BookId, Book)] = {
    val bookEntity = BookEntity(None, book.title, book.isbn, None, None, book.pages)
    val authorEntity = AuthorEntity(None, book.author.name)
    val publisherEntity = PublisherEntity(None, book.publisher.name)

    repository.save(bookEntity, authorEntity, publisherEntity).map {
      case (author, book, publisher) => (BookId(book.id.get), mapToBook((book, author, publisher)))
    }
  }

  override def getByTitle(title: String): IO[Option[Book]] = repository.getByTitle(title).map(_.map(mapToBook))

  private def mapToBook(tuple: (BookEntity, AuthorEntity, PublisherEntity)): Book = tuple match {
    case (book, author, publisher) =>
      Book(
        book.title,
        book.isbn,
        Author(author.name),
        Publisher(publisher.name),
        book.pages
      )
  }
}
