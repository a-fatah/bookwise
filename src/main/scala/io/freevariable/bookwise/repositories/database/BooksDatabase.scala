package io.freevariable.bookwise.repositories.database

import cats.effect.IO
import io.freevariable.bookwise.repositories.entites.BookEntity
import io.freevariable.bookwise.repositories.tables.BooksTable
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait BooksDatabase {
  def all(): IO[Seq[BookEntity]]

  def byId(id: Long): IO[Option[BookEntity]]

  def create(book: BookEntity): IO[BookEntity]

  def update(book: BookEntity): IO[BookEntity]

  def delete(id: Long): IO[Unit]
}

class SlickBooksDatabase(val profile: JdbcProfile)(implicit ec: ExecutionContext) extends BooksDatabase with SlickDbComponent {
  import profile.api._

  private val books = TableQuery[BooksTable]

  def all(): IO[Seq[BookEntity]] = IO.fromFuture(IO(db.run(books.result)))

  def byId(id: Long): IO[Option[BookEntity]] = IO.fromFuture(IO(db.run(books.filter(_.id === id).result.headOption)))

  def create(book: BookEntity): IO[BookEntity] = IO.fromFuture(IO(db.run(books returning books += book)))

  def update(book: BookEntity): IO[BookEntity] = IO.fromFuture(IO(db.run(books.filter(_.id === book.id).update(book).map(_ => book))))

  def delete(id: Long): IO[Unit] = IO.fromFuture(IO(db.run(books.filter(_.id === id).delete).map(_ => ())))

}
