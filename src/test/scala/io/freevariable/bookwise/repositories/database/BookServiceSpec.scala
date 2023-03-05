package io.freevariable.bookwise.repositories.database

import cats.effect.IO
import cats.implicits.catsSyntaxTuple2Semigroupal
import io.freevariable.bookwise._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
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

  "BookService" should "pass" in {

    val numbers = Gen.listOf(arbitraryBook)

    forAll(numbers) {
      case list => println(list)
    }

    assert(true)
  }

  "BookService" should "return all books" in {

    // import cats effect unsafe global
    import cats.effect.unsafe.implicits.global

    bookService.runMigrations().unsafeRunSync()

    val booksGen: Gen[List[Book]] = Gen.listOf(arbitraryBook)


    forAll(booksGen) { books =>

      println(books)

      import scala.concurrent.ExecutionContext.Implicits.global

      val insertActions = books.map { case book =>
        bookService.create(book)
      }

      // Run the insertActions in a transaction
      val insertsCombined: IO[List[Book]] = insertActions.foldLeft(IO.pure(List.empty[Book])) { (acc, action) =>
        for {
          seq <- acc
          book <- action
        } yield seq :+ book
      }


      val result = insertsCombined.flatMap { addedBooks =>
        val allBooks = bookService.getAll()

        (allBooks, IO.pure(addedBooks)).tupled
      }

      implicit val runtime: cats.effect.unsafe.IORuntime = cats.effect.unsafe.implicits.global

      val (allBooks, newBooks) = result.unsafeRunSync()

      if(!newBooks.isEmpty) {
        assert(allBooks.containsSlice(newBooks))
      }

    }
  }

}