package io.freevariable.bookwise

import cats.effect.IO

import scala.concurrent.ExecutionContext

trait BookService {

  def getAll(): IO[Seq[Book]]
  def get(id: BookId): IO[Option[Book]]
  def create(book: Book)(implicit ec: ExecutionContext): IO[(BookId, Book)]
  def getByTitle(title: String): IO[Option[Book]]
}


class BookServiceImpl(private val repository: BookRepository) extends BookService {

  override def getAll(): IO[Seq[Book]] = repository.all() flatMap { seq =>
    IO(seq.map { case (book, author) => Book(book.title, Author(author.name), book.pages) })
  }

  override def get(id: BookId): IO[Option[Book]] = repository.get(id.value) flatMap { opt =>
    IO(opt.map { case (book, author) => Book(book.title, Author(author.name), book.pages) })
  }

  override def create(book: Book)(implicit ec: ExecutionContext): IO[(BookId, Book)] = {
    repository.save(BookEntity(None, book.title, 0, book.pages), AuthorEntity(None, book.author.name)).map {
      case (author, book) => (BookId(book.id.get), Book(book.title, Author(author.name), book.pages))
    }
  }

  override def getByTitle(title: String): IO[Option[Book]] = {

    repository.getByTitle(title) flatMap { opt =>
      IO(opt.map { case (book, author) => Book(book.title, Author(author.name), book.pages) })
    }
  }
}
