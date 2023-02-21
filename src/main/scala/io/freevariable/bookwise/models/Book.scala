package io.freevariable.bookwise.models

case class Book(title: String, author: Author, pages: Int)

case class Author(name: String)
