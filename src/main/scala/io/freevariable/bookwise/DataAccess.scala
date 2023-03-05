package io.freevariable.bookwise

import cats.effect.IO
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


trait DatabaseProvider {
  val profile: JdbcProfile

  import profile.api._

  val db: Database
}

trait BooksSchema { this: DatabaseProvider =>

  import profile.api._

  class BooksTable (tag: Tag) extends Table[BookEntity](tag, "books") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def authorId = column[Long]("author_id")
    def pages = column[Int]("pages")

    def * = (id.?, title, authorId, pages) <> (BookEntity.tupled, BookEntity.unapply)
  }

  class AuthorsTable(tag: Tag) extends Table[AuthorEntity](tag, "authors") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name) <> (AuthorEntity.tupled, AuthorEntity.unapply)
  }

  val repository = new BookRepositoryImpl

  def runMigrations(): IO[Unit] = {
    val liquibase = new Liquibase("db/changelog/changelog-master.xml",
      new ClassLoaderResourceAccessor(), new JdbcConnection(db.source.createConnection()))
    IO(liquibase.update(""))
  }

  val books = TableQuery[BooksTable]
  val authors = TableQuery[AuthorsTable]

  trait BookRepository {
    def all(): IO[Seq[(BookEntity, AuthorEntity)]]
    def get(id: Long): IO[Option[(BookEntity, AuthorEntity)]]

    def save(book: BookEntity, author: AuthorEntity)(implicit ec: ExecutionContext): IO[(AuthorEntity, BookEntity)]
  }

  class BookRepositoryImpl extends BookRepository {
    def get(id: Long) = {
      val result = books.join(authors).on(_.authorId === _.id).filter(_._1.id === id).result
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

}

class BookService {
  self: BooksSchema with DatabaseProvider =>

  def getAll(): IO[Seq[Book]] = repository.all() flatMap { seq =>
    IO(seq.map { case (book, author) => Book(book.title, Author(author.name), book.pages) })
  }

  def get(id: Long): IO[Option[Book]] = repository.get(id) flatMap { opt =>
    IO(opt.map { case (book, author) => Book(book.title, Author(author.name), book.pages) })
  }

  def create(book: Book)(implicit ec: ExecutionContext): IO[Book] = {
    repository.save(BookEntity(None, book.title, 0, book.pages), AuthorEntity(None, book.author.name)).map {
      case (author, book) => Book(book.title, Author(author.name), book.pages)
    }
  }

}
