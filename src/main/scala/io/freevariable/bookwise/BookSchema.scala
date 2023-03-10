package io.freevariable.bookwise

import cats.effect.IO
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor


trait BooksSchema { this: DatabaseProvider =>

  import profile.api._

  class AuthorsTable(tag: Tag) extends Table[AuthorEntity](tag, "authors") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id.?, name) <> (AuthorEntity.tupled, AuthorEntity.unapply)
  }

  class PublishersTable(tag: Tag) extends Table[PublisherEntity](tag, "publishers") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id.?, name) <> (PublisherEntity.tupled, PublisherEntity.unapply)
  }

  val books = TableQuery[BooksTable]
  val authors = TableQuery[AuthorsTable]
  val publishers = TableQuery[PublishersTable]

  class BooksTable (tag: Tag) extends Table[BookEntity](tag, "books") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def isbn = column[String]("isbn")
    def authorId = column[Long]("author_id")
    def publisherId = column[Long]("publisher_id")
    def pages = column[Int]("pages")

    def author = foreignKey("author_fk", authorId, authors)(_.id)
    def publisher = foreignKey("publisher_fk", publisherId, publishers)(_.id)

    def * = (id.?, title, isbn, authorId.?, publisherId.?, pages) <> (BookEntity.tupled, BookEntity.unapply)
  }

  // use liquibase to run migrations on production database
  def runMigrations(): IO[Unit] = {
    val liquibase = new Liquibase("db/changelog/changelog-master.xml",
      new ClassLoaderResourceAccessor(), new JdbcConnection(db.source.createConnection()))
    IO(liquibase.update(""))
  }


  // TODO: add createSchema method to create the schema if it doesn't exist

}
