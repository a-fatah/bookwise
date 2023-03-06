package io.freevariable.bookwise

import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits.toSemigroupKOps
import com.comcast.ip4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._


object Server extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    trait PostgresDatabaseProvider extends DatabaseProvider {
      override val profile = slick.jdbc.PostgresProfile
      import profile.api._
      val db: Database = Database.forConfig("db.postgres")
    }

    trait H2DatabaseProvider extends DatabaseProvider {
      override val profile = slick.jdbc.H2Profile
      import profile.api._
      val db: Database = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }

    trait PostgresModule extends PostgresDatabaseProvider with BooksSchema

    trait H2Module extends H2DatabaseProvider with BooksSchema

    val bookService = new BookServiceImpl with PostgresModule

    val bookServiceH2 = new BookServiceImpl with H2Module

    println(bookService.db == bookServiceH2.db)


    println("Running migrations...")
    bookServiceH2.runMigrations().unsafeRunSync()
    println("Migrations complete.")

    bookServiceH2.getAll().unsafeRunSync().foreach(println)

    val bookRoutes = HttpRoutes.of[IO] {

      case GET -> Root / "books" =>
        bookServiceH2.getAll().flatMap(books =>
          Ok(books.asJson)
        )

      case GET -> Root / "books" / LongVar(id) =>
        bookServiceH2.get(BookId(id)).flatMap {
          case Some(book) => Ok(book.asJson)
          case None => NotFound()
        }

    }

    val app = bookRoutes.orNotFound

    val server = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build

    server.use(_ => IO.never).as(ExitCode.Success)
  }
}
