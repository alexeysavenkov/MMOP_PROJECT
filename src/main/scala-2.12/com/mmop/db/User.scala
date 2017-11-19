package com.mmop.db

import MmopDatabase._
import scalikejdbc._
import com.mmop.utils.RandomString

class User(val fields: Map[String, Any]) extends AbstractModel {

  def phoneNumber = fields("phone").asInstanceOf[String]

  def generateNewSmsCode(): String = {
    val smsCode = RandomString(possibleCharacters = "0123456789abcdefghijklmnopqrstuvwxyz", length = 5)
    MmopDatabase.withSession(implicit db => {
      sql"UPDATE `User` SET smsLoginCode = ${smsCode}, timeSmsSent = NOW() WHERE phone = ${phoneNumber}".executeUpdate().apply()
      smsCode
    })
  }
}

object User {


  def byPhoneAndPassword(phoneNumber: String, password: String): Option[User] = {
    MmopDatabase.withReadOnlySession(implicit db => {
      sql"SELECT * FROM `User` WHERE phone = ${phoneNumber} AND password = ${password}"
        .map(_.toMap()).single().apply().map(new User(_))
    })
  }

}
