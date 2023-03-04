package io.freevariable.bookwise

import cats.effect.IO
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import slick.jdbc.JdbcProfile


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

  val tableQuery = TableQuery[BooksTable]

  trait BookRepository {
    def all(): IO[Seq[BookEntity]]
    def get(id: Long): IO[Option[BookEntity]]
  }

  class BookRepositoryImpl extends BookRepository {
    def get(id: Long) = IO.fromFuture(IO(db.run(tableQuery.filter(_.id === id).result.headOption)))

    override def all(): IO[Seq[BookEntity]] = IO.fromFuture(IO(db.run(tableQuery.result)))
  }

}

class BookService {
  self: BooksSchema with DatabaseProvider =>

  def getAll(): IO[Seq[BookEntity]] = repository.all()
  def get(id: Long): IO[Option[BookEntity]] = repository.get(id)
}
