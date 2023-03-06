package io.freevariable.bookwise

import cats.effect.IO

import scala.concurrent.ExecutionContext

trait BookRepository {
  def all(): IO[Seq[(BookEntity, AuthorEntity)]]

  def get(id: Long): IO[Option[(BookEntity, AuthorEntity)]]

  def getByTitle(title: String): IO[Option[(BookEntity, AuthorEntity)]]

  def save(book: BookEntity, author: AuthorEntity)(implicit ec: ExecutionContext): IO[(AuthorEntity, BookEntity)]
}

class BookRepositoryImpl extends BookRepository {
  self: BooksSchema with DatabaseProvider =>

  import profile.api._

  val books = TableQuery[BooksTable]
  val authors = TableQuery[AuthorsTable]

  override def get(id: Long) = {
    val result = books.join(authors).on(_.authorId === _.id).filter(_._1.id === id).result
    IO.fromFuture(IO(db.run(result))).map(_.headOption)
  }

  override def getByTitle(title: String): IO[Option[(BookEntity, AuthorEntity)]] = {
    val result = books.join(authors).on(_.authorId === _.id).filter(_._1.title === title).result
    IO.fromFuture(IO(db.run(result))).map(_.headOption)
  }

  override def all(): IO[Seq[(BookEntity, AuthorEntity)]] = {
    val result = books.join(authors).on(_.authorId === _.id).result
    IO.fromFuture(IO(db.run(result)))
  }

  override def save(book: BookEntity, author: AuthorEntity)(implicit ec: ExecutionContext): IO[(AuthorEntity, BookEntity)] = {
    val result = for {
      a <- (authors returning authors.map(_.id) into ((author, id) => author.copy(id = Some(id)))) += author
      b <- (books returning books.map(_.id) into ((book, id) => book.copy(id = Some(id)))) += book.copy(authorId = a.id.get)
    } yield (a, b)

    IO.fromFuture(IO(db.run(result)))
  }
}
