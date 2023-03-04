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

    trait PostgresDatabaseLayer extends DatabaseLayer {
      override val profile = slick.jdbc.PostgresProfile
      import profile.api._
      val db: Database = Database.forConfig("db.postgres")
    }

    trait PostgresDatabaseModule extends BooksDatabase with PostgresDatabaseLayer

    val booksModule = new BooksDatabaseModule with PostgresDatabaseModule

    println("Running migrations...")
    booksModule.runMigrations().unsafeRunSync()
    println("Migrations complete.")

    // initialize the server
    val bookRoutes = HttpRoutes.of[IO] {
      case GET -> Root / "books" => {
        val books = booksModule.getAll().unsafeRunSync()
        Ok(books.asJson)
      }
      case GET -> Root / "books" / IntVar(id) => {
        val book = booksModule.get(id).unsafeRunSync()
        book match {
          case Some(book) => Ok(book.asJson)
          case None => NotFound()
        }
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
