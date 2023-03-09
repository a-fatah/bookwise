package io.freevariable.bookwise

case class BookEntity(id: Option[Long] = None, title: String, isbn: String, authorId: Option[Long] = None, publisherId: Option[Long] = None , pages: Int)
case class AuthorEntity(id: Option[Long] = None, name: String)
case class PublisherEntity(id: Option[Long] = None, name: String)
