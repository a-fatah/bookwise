package io.freevariable.bookwise.repositories.database

import cats.effect.IO
import io.freevariable.bookwise.db.{BookRepositoryImpl, BookSchema, DatabaseProvider}
import io.freevariable.bookwise.model.{Book, BookId}
import io.freevariable.bookwise.service.BookServiceImpl
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.contain
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global


class BookServiceSpec extends AnyFlatSpec with ScalaFutures with BeforeAndAfterEach with ScalaCheckPropertyChecks with ArbitraryInstances {

  trait H2DatabaseProvider extends DatabaseProvider {
    override val profile = slick.jdbc.H2Profile
    import profile.api._
    val db: Database = Database.forURL("jdbc:h2:mem:books;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true", driver = "org.h2.Driver")
  }

  trait H2Module extends H2DatabaseProvider with BookSchema
  val bookRepository = new BookRepositoryImpl with H2Module
  val bookService = new BookServiceImpl(bookRepository)

  override def beforeEach(): Unit = {
    import cats.effect.unsafe.implicits.global
    bookRepository.runMigrations().unsafeRunSync()
  }

  "BookService" should "return all books" in {
    val booksGen: Gen[List[Book]] = Gen.listOf(arbitraryBook)

    forAll(booksGen) { books =>
      // Create a list of IO actions to insert books
      val insertActions = books.map(bookService.create)

      // Run inserts in parallel
      val inserts: IO[List[BookId]] = IO.parSequenceN(4)(insertActions)
      val allBooks = bookService.getAll

      for {
        books <- inserts
        allBooks <- bookService.getAll
      } yield {
        if (books.nonEmpty)
          allBooks should contain allElementsOf books
      }

    }
  }

  "BookService" should "return a book by id" in {
    for {
      book <- arbitraryBook
      id <- bookService.create(book)
      maybeBook <- bookService.get(id)
    } yield {
      maybeBook should contain(book)
    }

  }


  "BookService" should "return a book by title" in {
    for {
      book <- arbitraryBook
      id <- bookService.create(book)
      maybeBook <- bookService.getByTitle(book.title)
    } yield {
      maybeBook should contain(book)
    }

  }

}
