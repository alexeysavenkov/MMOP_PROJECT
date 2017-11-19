package com.mmop

import scalikejdbc._

object SmsHandler {

  // initialize JDBC driver & connection pool
  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.add("turbosms", "jdbc:mysql://94.249.146.189/users", "alexeysavenkov", "zzzz1234")
  val connection = ConnectionPool.get("turbosms")

  def sendSms(phoneNumber: String, message: String) : Unit = {
    using(DB(connection.borrow())) { db =>
      db.localTx { implicit session =>
        sql"INSERT INTO alexeysavenkov (number, sign, message, wappush) VALUES (${phoneNumber}, 'Msg', ${message},'')".execute().apply()
      }
    }
  }

  def sendLoginCode(phoneNumber: String, loginCode: String) : Unit = {
    sendSms(phoneNumber, s"Your login code is $loginCode. Welcome to MMOP E-banking!")
  }
}