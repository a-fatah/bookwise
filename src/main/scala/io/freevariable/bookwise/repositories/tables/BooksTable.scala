package io.freevariable.bookwise.repositories.tables

import io.freevariable.bookwise.repositories.entites.{AuthorEntity, BookEntity}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ForeignKeyQuery


class BooksTable(tag: Tag) extends Table[BookEntity](tag, "books") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def authorId = column[Long]("author_id")
  def pages = column[Int]("pages")

  def * = (id.?, title, authorId, pages) <> (BookEntity.tupled, BookEntity.unapply)

  def author: ForeignKeyQuery[AuthorsTable, AuthorEntity] =
    foreignKey("author_fk", authorId, TableQuery[AuthorsTable])(_.id)
}

class AuthorsTable(tag: Tag) extends Table[AuthorEntity](tag, "authors") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (AuthorEntity.tupled, AuthorEntity.unapply)
}