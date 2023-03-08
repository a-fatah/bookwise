package io.freevariable.bookwise

import cats.effect.IO

import scala.concurrent.ExecutionContext

trait BookRepository {
  def all(): IO[Seq[(BookEntity, AuthorEntity, PublisherEntity)]]

  def get(id: Long): IO[Option[(BookEntity, AuthorEntity, PublisherEntity)]]

  def getByTitle(title: String): IO[Option[(BookEntity, AuthorEntity, PublisherEntity)]]

  def save(book: BookEntity, author: AuthorEntity, publisherEntity: PublisherEntity)(implicit ec: ExecutionContext): IO[(AuthorEntity, BookEntity, PublisherEntity)]
}

class BookRepositoryImpl extends BookRepository {
  self: BooksSchema with DatabaseProvider =>

  import profile.api._

  override def get(id: Long): IO[Option[(BookEntity, AuthorEntity, PublisherEntity)]] = {
    val query = for {
      book <- books.filter(_.id === id)
      author <- book.author
      publisher <- book.publisher
    } yield (book, author, publisher)

    IO.fromFuture(IO(db.run(query.result.headOption)))
  }

  override def getByTitle(title: String): IO[Option[(BookEntity, AuthorEntity, PublisherEntity)]] = {
    val query = for {
      book <- books.filter(_.title === title)
      author <- book.author
      publisher <- book.publisher
    } yield (book, author, publisher)

    IO.fromFuture(IO(db.run(query.result.headOption)))
  }

  override def all(): IO[Seq[(BookEntity, AuthorEntity, PublisherEntity)]] = {
    val query = for {
      book <- books
      author <- book.author
      publisher <- book.publisher
    } yield (book, author, publisher)

    IO.fromFuture(IO(db.run(query.result)))
  }

  override def save(book: BookEntity, author: AuthorEntity, publisherEntity: PublisherEntity)(implicit ec: ExecutionContext): IO[(AuthorEntity, BookEntity, PublisherEntity)] = {
    val query = for {
      author <- (authors returning authors.map(_.id) into ((author, id) => author.copy(id = Some(id)))) += author
      book <- (books returning books.map(_.id) into ((book, id) => book.copy(id = Some(id)))) += book.copy(authorId = author.id.get)
      publisher <- (publishers returning publishers.map(_.id) into ((publisher, id) => publisher.copy(id = Some(id)))) += publisherEntity
    } yield (author, book, publisher)

    IO.fromFuture(IO(db.run(query)))
  }
}
