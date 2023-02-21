package io.freevariable.bookwise.repositories.database
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext

trait SlickDbComponent {
  val db: Database = Database.forConfig("books")
  implicit val ec: ExecutionContext = ExecutionContext.global
}
