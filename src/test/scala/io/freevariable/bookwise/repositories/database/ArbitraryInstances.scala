package io.freevariable.bookwise.repositories.database

import io.freevariable.bookwise.{Author, AuthorEntity, Book, BookEntity}
import org.scalacheck.{Arbitrary, Gen}

trait ArbitraryInstances {
  implicit val arbitraryBookEntity: Arbitrary[BookEntity] = Arbitrary {
    for {
      title <- Gen.alphaStr
      author <- Gen.posNum[Long]
      pages <- Gen.posNum[Int]
    } yield BookEntity(None, title, author, pages)
  }

  implicit val arbitraryAuthorEntity: Arbitrary[AuthorEntity] = Arbitrary {
    for {
      name <- Gen.alphaStr
    } yield AuthorEntity(None, name)
  }

  implicit val arbitraryBook: Gen[Book] = for {
    author <- arbitraryAuthorEntity.arbitrary
    book <- arbitraryBookEntity.arbitrary
  } yield Book(book.title, Author(author.name), book.pages)

}
