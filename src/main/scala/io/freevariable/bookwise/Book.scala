package io.freevariable.bookwise

case class Book(title: String, isbn: String, author: Author, publisher: Publisher, pages: Int)

case class BookId(value: Long) extends AnyVal
case class Author(name: String)
case class Publisher(name: String)
