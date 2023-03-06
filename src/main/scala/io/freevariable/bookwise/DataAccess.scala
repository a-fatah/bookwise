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

  def runMigrations(): IO[Unit] = {
    val liquibase = new Liquibase("db/changelog/changelog-master.xml",
      new ClassLoaderResourceAccessor(), new JdbcConnection(db.source.createConnection()))
    IO(liquibase.update(""))
  }

}
