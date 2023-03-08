package io.freevariable.bookwise

// TODO: make authorId and publisherId optional
case class BookEntity(id: Option[Long], title: String, isbn: String, authorId: Long, publisherId: Long, pages: Int)
case class AuthorEntity(id: Option[Long], name: String)
case class PublisherEntity(id: Option[Long], name: String)
