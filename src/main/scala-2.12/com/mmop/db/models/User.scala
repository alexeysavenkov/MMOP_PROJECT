package com.mmop.db.models

import java.sql.Timestamp

import com.mmop.db.{AbstractModel, MmopDatabase}
import com.mmop.utils.RandomString
import scalikejdbc._

class User(val fields: Map[String, Any]) extends AbstractModel {

  def id = getField[Long]("id")
  def phoneNumber = getField[String]("phone")
  def smsCodeOpt = getFieldOption[String]("smsLoginCode")
  def smsCode = getField[String]("smsLoginCode")
  def publicToken = getField[String]("publicToken")
  def timeSmsSent = getField[Timestamp]("timeSmsSent")

  def generateNewSmsCode(): String = {
    val smsCode = RandomString(possibleCharacters = "0123456789abcdefghijklmnopqrstuvwxyz", length = 5)
    MmopDatabase.withSession(implicit db => {
      sql"UPDATE `User` SET smsLoginCode = ${smsCode}, timeSmsSent = NOW() WHERE phone = ${phoneNumber}".executeUpdate().apply()
      smsCode
    })
  }

  def clearSmsCode(): Unit = {
    MmopDatabase.withSession(implicit db => {
      sql"UPDATE `User` SET smsLoginCode = NULL WHERE phone = ${phoneNumber}".executeUpdate().apply()
    })
  }
}

object User {

  def byPhone(phoneNumber: String): Option[User] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `User` WHERE phone = ${phoneNumber}"
        .map(_.toMap()).single().apply().map(new User(_))
    })
  }

  def byPhoneAndPassword(phoneNumber: String, password: String): Option[User] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `User` WHERE phone = ${phoneNumber} AND password = ${password}"
        .map(_.toMap()).single().apply().map(new User(_))
    })
  }

  def byPublicToken(publicToken: String): Option[User] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `User` WHERE publicToken = ${publicToken}"
        .map(_.toMap()).single().apply().map(new User(_))
    })
  }

}
