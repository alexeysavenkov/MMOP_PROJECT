package com.mmop.db

import scalikejdbc.{ConnectionPool, DB, DBSession, using}

object MmopDatabase {
  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.add("mmop", "jdbc:mysql://mmop-db.cs9tngvh7qnb.us-east-2.rds.amazonaws.com/mmop", "admin", "zzzz1234")
  implicit val connection = ConnectionPool.get("mmop")

  def withSession[T](f: DBSession => T): T = {
    using(DB(connection.borrow())) { db =>
      db.localTx { implicit session =>
        f(session)
      }
    }
  }

  def withReadOnlySession[T](f: DBSession => T): T = {
    using(DB(connection.borrow())) { db =>
      db.readOnly { implicit session =>
        f(session)
      }
    }
  }
}
