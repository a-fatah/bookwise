package io.freevariable.bookwise.service

import cats.effect.IO
import io.freevariable.bookwise.db.BookRepository
import io.freevariable.bookwise.model.{Book, BookId}

import scala.concurrent.ExecutionContext

trait BookService {
  implicit val ec: ExecutionContext

  def getAll: IO[Seq[Book]]
  def get(id: BookId): IO[Option[Book]]
  def create(book: Book): IO[BookId]
  def getByTitle(title: String): IO[Option[Book]]
}


class BookServiceImpl(private val repository: BookRepository) extends BookService {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  override def getAll: IO[Seq[Book]] = repository.all()

  override def get(id: BookId): IO[Option[Book]] = repository.get(id)

  override def create(book: Book): IO[BookId] = {
    repository.save(book)
  }

  override def getByTitle(title: String): IO[Option[Book]] = repository.getByTitle(title)
}
