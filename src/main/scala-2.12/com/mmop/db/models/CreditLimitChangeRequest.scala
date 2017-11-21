package com.mmop.db.models

import java.sql.Timestamp

import com.mmop.db.{AbstractModel, MmopDatabase}
import org.json4s.JObject
import org.json4s.JsonAST.{JDecimal, JInt}
import scalikejdbc._

object CreditLimitChangeRequest {

  def create(account : Account, newCreditLimit : BigDecimal) : Unit = {
    MmopDatabase.withSession(implicit session => {

      sql"""INSERT INTO `CreditLimitChangeRequest`(account, newCreditLimit)
            VALUES (${account.id}, ${newCreditLimit})
         """.execute().apply()
    })
  }

}

