package io.freevariable.bookwise.repositories.database

import io.freevariable.bookwise.{Author, AuthorEntity, Book, BookEntity, Publisher, PublisherEntity}
import org.scalacheck.{Arbitrary, Gen}

trait ArbitraryInstances {
  implicit val arbitraryBookEntity: Arbitrary[BookEntity] = Arbitrary {
    for {
      title <- Gen.alphaStr
      isbn <- Gen.alphaStr
      author <- Gen.posNum[Long]
      publisher <- Gen.posNum[Long]
      pages <- Gen.posNum[Int]
    } yield BookEntity(None, title, isbn, author, publisher, pages)
  }

  implicit val arbitraryAuthorEntity: Arbitrary[AuthorEntity] = Arbitrary {
    for {
      name <- Gen.alphaStr
    } yield AuthorEntity(None, name)
  }

  implicit val arbitraryPublisherEntity: Arbitrary[PublisherEntity] = Arbitrary {
    for {
      name <- Gen.alphaStr
    } yield PublisherEntity(None, name)
  }

  implicit val arbitraryBook: Gen[Book] = for {
    author <- arbitraryAuthorEntity.arbitrary
    publisher <- arbitraryPublisherEntity.arbitrary
    book <- arbitraryBookEntity.arbitrary
  } yield Book(book.title, book.isbn, Author(author.name), Publisher(publisher.name), book.pages)

}
