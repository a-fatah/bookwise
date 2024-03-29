package io.freevariable.bookwise.db

case class BookEntity(
   id: Option[Long],
   title: String,
   isbn: String,
   authorId: Option[Long],
   publisherId: Option[Long],
   pages: Int
)

case class AuthorEntity(id: Option[Long] = None, name: String)

case class PublisherEntity(id: Option[Long] = None, name: String)

