package com.mmop.db.models

import java.sql.Timestamp

import com.mmop.db.{AbstractModel, MmopDatabase}
import org.json4s.JObject
import org.json4s.JsonAST.{JDecimal, JInt}
import scalikejdbc._

class Transaction(val fields: Map[String, Any]) extends AbstractModel {

  def id = getField[Long]("id")
  def automaticTransactionId = getFieldOption[Int]("automaticTransactionId")
  def sourceAccount = getField[Int]("sourceAccount")
  def destinationAccount = getField[Int]("destinationAccount")
  def amount : BigDecimal = getFieldDecimal("amount")
  def timeCommitted = getField[Timestamp]("timeCommitted")

  //def asOutput: JObject = JObject("id" -> JInt(id), "amount" -> JDecimal(amount), "creditLimit" -> JDecimal(creditLimit))

}

object Transactions {

  def create(notVerifiedSrcAccount : Account, destAccount : Account, amount : BigDecimal, automaticTransaction: Option[AutomaticTransaction] = None) : Either[String, Unit] = {
    MmopDatabase.withSession(implicit session => {

      // To avoid race condition;
      // Efficiency can be improved (by locking only TWO accounts instead of ALL accounts)
      synchronized {

        val verifiedSrcAccountOpt: Option[Account] = notVerifiedSrcAccount.refresh()

        verifiedSrcAccountOpt match {
          case Some(verifiedSrcAccount) =>
            if (amount <= 0) {
              Left("Cannot send non-positive amount of money")
            } else if (verifiedSrcAccount.amount + verifiedSrcAccount.creditLimit < amount) {
              Left(s"Source account does not have enough money (${verifiedSrcAccount.amount} needed; $amount present)")
            } else {
              // Transactions are atomic
              sql"""START TRANSACTION;""".execute().apply()
              sql"""UPDATE Account SET amount = amount + ${amount} WHERE id = ${destAccount.id};""".execute().apply()
              sql"""UPDATE Account SET amount = amount - ${amount} WHERE id = ${verifiedSrcAccount.id};""".execute().apply()
              sql"""
                  INSERT INTO `Transaction`(sourceAccount, destinationAccount, amount, automaticTransactionId)
                  VALUES (${verifiedSrcAccount.id}, ${destAccount.id}, ${amount}, ${automaticTransaction.map(_.id)});
                """.execute().apply()
              sql""" COMMIT """.execute().apply()


              Right()
            }
          case None =>
            Left(s"Account ${notVerifiedSrcAccount.id} does not exist")
        }

      }
    })
  }

  def byAccount(account: Account): Seq[Transaction] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `Transaction` WHERE sourceAccount = ${account.id} OR destinationAccount = ${account.id} ORDER BY timeCommitted DESC"
        .map(_.toMap()).list().apply().map(new Transaction(_))
    })
  }

}

