package io.freevariable.bookwise

import cats.effect._
import com.comcast.ip4s._
import io.circe.generic.auto._
import io.circe.syntax._
import io.freevariable.bookwise.models.{Author, Book}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._


object Server extends IOApp {

  val books: Seq[Book] = List(
    Book("To Kill a Mockingbird", Author("Harper Lee"), 281),
    Book("The Great Gatsby", Author("F. Scott Fitzgerald"), 180),
    Book("Pride and Prejudice", Author("Jane Austen"), 279),
    Book("1984", Author("George Orwell"), 328),
    Book("The Catcher in the Rye", Author("J.D. Salinger"), 234),
    Book("One Hundred Years of Solitude", Author("Gabriel Garcia Marquez"), 417),
    Book("Brave New World", Author("Aldous Huxley"), 288),
    Book("The Hobbit", Author("J.R.R. Tolkien"), 304),
    Book("To the Lighthouse", Author("Virginia Woolf"), 209),
    Book("The Picture of Dorian Gray", Author("Oscar Wilde"), 254)
  )

  def run(args: List[String]): IO[ExitCode] = {

    val bookRoutes = HttpRoutes.of[IO] {
      case GET -> Root / "books" =>
        Ok(books.asJson)
      case GET -> Root / "books" / IntVar(id) =>
        books.lift(id) match {
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