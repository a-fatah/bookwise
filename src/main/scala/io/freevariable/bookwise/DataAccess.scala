package io.freevariable.bookwise

import cats.effect.IO
import slick.jdbc.JdbcProfile


trait DatabaseLayer {
  val profile: JdbcProfile

  import profile.api._

  val db: Database
}

trait BooksDatabase { this: DatabaseLayer =>

  import profile.api._

  class BooksTable (tag: Tag) extends Table[BookEntity](tag, "books") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def authorId = column[Long]("author_id")
    def pages = column[Int]("pages")

    def * = (id.?, title, authorId, pages) <> (BookEntity.tupled, BookEntity.unapply)
  }

  class AuthorsTable(tag: Tag) extends Table[AuthorEntity](tag, "authors") with HasIdColumn[Long] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name) <> (AuthorEntity.tupled, AuthorEntity.unapply)
  }

  val tableQuery = TableQuery[BooksTable]

  trait BooksRepository {
    def all(): IO[Seq[BookEntity]]
  }

  class BooksRepositoryImpl extends BooksRepository {
    override def all(): IO[Seq[BookEntity]] = IO.fromFuture(IO(db.run(tableQuery.result)))
  }

}

class BooksDatabaseModule {
  self: BooksDatabase with DatabaseLayer =>
  val repository = new BooksRepositoryImpl

  def getAll(): IO[Seq[BookEntity]] = repository.all()
}
