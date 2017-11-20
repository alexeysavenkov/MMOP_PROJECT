package com.mmop.db.models

import com.mmop.db.{AbstractModel, MmopDatabase}
import org.json4s.JObject
import org.json4s.JsonAST.{JDecimal, JInt}
import scalikejdbc._

class Account(val fields: Map[String, Any]) extends AbstractModel {

  def id = getField[Long]("id")
  def userId = getField[Int]("userId")
  def amount : BigDecimal = getFieldDecimal("amount")
  def creditLimit : BigDecimal = getFieldDecimal("creditLimit")

  def asOutput: JObject = JObject("id" -> JInt(id), "amount" -> JDecimal(amount), "creditLimit" -> JDecimal(creditLimit))

  def delete(): Unit = {
    MmopDatabase.withSession(implicit db => {
      sql"DELETE FROM `Account` WHERE id = ${id}".executeUpdate().apply()
    })
  }

  def refresh(): Option[Account] = {
    Accounts.byId(id.toInt)
  }
}

object Accounts {

  def byUser(user : User): Seq[Account] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `Account` WHERE userId = ${user.id}"
        .map(_.toMap()).list().apply().map(new Account(_))
    })
  }

  def createForUser(user : User): Seq[Account] = {
    MmopDatabase.withSession(implicit db => {
      sql"INSERT INTO `Account`(userId) VALUES (${user.id})".executeUpdate().apply()
    })
    Accounts.byUser(user)
  }

  def byId(id: Int): Option[Account] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `Account` WHERE id = ${id}"
        .map(_.toMap()).single().apply().map(new Account(_))
    })
  }
}

