package io.freevariable.bookwise.repositories.entites

case class BookEntity(id: Option[Long], title: String, authorId: Long, pages: Int)
case class AuthorEntity(id: Option[Long], name: String)

