package io.freevariable.bookwise

import slick.jdbc.JdbcProfile

trait DatabaseProvider {
  val profile: JdbcProfile

  import profile.api._

  val db: Database
}
