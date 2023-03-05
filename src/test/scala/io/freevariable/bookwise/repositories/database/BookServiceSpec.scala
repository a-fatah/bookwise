package io.freevariable.bookwise.repositories.database

import cats.effect.IO
import cats.implicits.{catsSyntaxParallelSequence1, catsSyntaxTuple2Parallel, catsSyntaxTuple2Semigroupal, toTraverseOps}
import io.freevariable.bookwise._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class BookServiceSpec extends AnyFlatSpec with ScalaFutures with ScalaCheckPropertyChecks {

  trait H2DatabaseProvider extends DatabaseProvider {
    override val profile = slick.jdbc.H2Profile
    import profile.api._
    val db: Database = Database.forURL("jdbc:h2:mem:books;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true", driver = "org.h2.Driver")
  }

  trait H2Module extends H2DatabaseProvider with BooksSchema

  val bookService = new BookService with H2Module


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

  "BookService" should "return all books" in {

    import cats.effect.unsafe.implicits.global

    bookService.runMigrations().unsafeRunSync()

    val booksGen: Gen[List[Book]] = Gen.listOf(arbitraryBook)

    forAll(booksGen) { books =>

      import scala.concurrent.ExecutionContext.Implicits.global

      val insertActions = books.map(bookService.create)

      // Run inserts in parallel
      val inserts: IO[List[Book]] = IO.parSequenceN(4)(insertActions)
      val allBooks = bookService.getAll()

      inserts.flatMap { insertedBooks =>
        allBooks.map { allBooks =>
          if (insertedBooks.nonEmpty)
            allBooks should contain allElementsOf insertedBooks
        }
      }

    }
  }

}
