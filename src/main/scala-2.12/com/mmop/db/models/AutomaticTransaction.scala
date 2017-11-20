package com.mmop.db.models

import java.sql.Timestamp

import com.mmop.db.{AbstractModel, MmopDatabase}
import org.json4s.JObject
import org.json4s.JsonAST.{JDecimal, JInt, JString}
import scalikejdbc._

class AutomaticTransaction(val fields: Map[String, Any]) extends AbstractModel {

  def id = getField[Long]("id")
  def sourceAccount = getField[Int]("sourceAccount")
  def destinationAccount = getField[Int]("destinationAccount")
  def amount = getFieldDecimal("amount")
  def intervalDays = getField[Int]("intervalDays")
  def timeLastTransaction = getFieldOption[Timestamp]("timeLastTransaction")

  def asOutput = JObject(
    "id" -> JInt(id),
    "destinationAccount" -> JInt(destinationAccount),
    "amount" -> JDecimal(amount),
    "intervalDays" -> JInt(intervalDays),
    "timeLastTransaction" -> JString(timeLastTransaction.toString)
  )

  def delete(): Unit = {
    MmopDatabase.withSession(implicit db => {
      sql"DELETE FROM `AutomaticTransaction` WHERE id = ${id}".executeUpdate().apply()
    })
  }
}

object AutomaticTransactions {

  def create(srcAccount : Account, destAccount : Account, amount : BigDecimal, intervalDays: Int) : Either[String, Unit] = {
    MmopDatabase.withSession(implicit session => {
      if(intervalDays <= 0) {
        Left("Interval cannot be non-positive")
      } else if (amount <= 0) {
        Left("Amount cannot be non-positive")
      } else {
        sql"""
              INSERT INTO `AutomaticTransaction`(sourceAccount, destinationAccount, amount, intervalDays)
              VALUES (${srcAccount.id}, ${destAccount.id}, ${amount}, ${intervalDays});
            """.executeUpdate().apply()
        Right()
      }
    })
  }

  def byAccount(account: Account): Seq[AutomaticTransaction] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `AutomaticTransaction` WHERE sourceAccount = ${account.id} OR destinationAccount = ${account.id}"
        .map(_.toMap()).list().apply().map(new AutomaticTransaction(_))
    })
  }

  def byId(id: Int): Option[AutomaticTransaction] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `AutomaticTransaction` WHERE id = ${id}"
        .map(_.toMap()).single().apply().map(new AutomaticTransaction(_))
    })
  }

}

