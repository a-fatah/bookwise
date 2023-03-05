package io.freevariable.bookwise

case class BookId(value: Long) extends AnyVal
case class Book(title: String, author: Author, pages: Int)

case class Author(name: String)
