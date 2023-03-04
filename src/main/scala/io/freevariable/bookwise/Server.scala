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

    trait PostgresModule extends PostgresDatabaseProvider with BooksSchema

    val bookService = new BookService with PostgresModule

    println("Running migrations...")
    bookService.runMigrations().unsafeRunSync()
    println("Migrations complete.")

    val bookRoutes = HttpRoutes.of[IO] {

      case GET -> Root / "books" =>
        bookService.getAll().flatMap(books =>
          Ok(books.asJson)
        )

      case GET -> Root / "books" / IntVar(id) =>
        bookService.get(id).flatMap {
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
