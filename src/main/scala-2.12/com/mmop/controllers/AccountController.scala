package com.mmop.controllers

import javax.inject.Inject

import com.mmop.db.models.{Accounts, CreditLimitChangeRequest, Transactions, User}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.json4s.JsonAST.JArray
import org.json4s.jackson.JsonMethods._

import scala.util.Try

class AccountController @Inject()(
                                     //exampleService: ExampleService
                                 ) extends Controller {

 get("/accounts") { request: Request =>
   val token = request.getParam("token")
   if(Seq(token).exists(x => x == null || x.isEmpty)) {
     response.badRequest("Token is empty")
   } else {
     User.byPublicToken(token) match {
       case Some(user) =>
         val accounts = Accounts.byUser(user)
         val output = JArray(accounts.map(_.asOutput).toList)
         response.ok(pretty(render(output)))
       case None =>
         response.badRequest("User not found")
     }
   }
 }

  put("/accounts") { request: Request =>
    val token = request.getParam("token")
    if(Seq(token).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Token is empty")
    } else {
      User.byPublicToken(token) match {
        case Some(user) =>
          val accounts = Accounts.createForUser(user)
          val output = JArray(accounts.map(_.asOutput).toList)
          response.ok(pretty(render(output)))
        case None =>
          response.badRequest("User not found")
      }
    }
  }

  delete("/accounts/:id") { request: Request =>
    val token = request.getParam("token")
    val accountId = request.getIntParam("id")
    if(Seq(token).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Token is empty")
    } else {
      User.byPublicToken(token) match {
        case Some(user) =>
          Accounts.byId(accountId) match {
            case Some(account) =>
              if(account.userId != user.id) {
                response.badRequest("Account does not belong to you")
              } else if(Math.abs(account.amount.toDouble) >= 0.01) {
                response.badRequest("Account amount is not zero")
              } else {
                account.delete()
                val allAccounts = Accounts.byUser(user)
                val output = JArray(allAccounts.map(_.asOutput).toList)
                response.ok(pretty(render(output)))
              }
            case None =>
              response.badRequest("Account does not exist")
          }
        case None =>
          response.badRequest("User not found")
      }
    }
  }

  post("/accounts/:id/credit-limit/:newCreditLimit") { request: Request =>
    val token = request.getParam("token")
    val accountId = request.getIntParam("id")
    val newCreditLimitStr = request.getParam("newCreditLimit")
    if(Seq(token, newCreditLimitStr).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Not all parameters specified")
    } else if(Try { assert( (newCreditLimitStr.toDouble % 0.01) > 0) } isFailure) {
      response.badRequest("Bad amount parameter")
    } else {
      // These nested expressions can be avoided by for-expression
      User.byPublicToken(token) match {
        case Some(user) =>
          Accounts.byId(accountId) match {
            case Some(account) if account.userId != user.id =>
              response.badRequest("Account does not belong to you")

            case Some(account) =>
              CreditLimitChangeRequest.create(account, BigDecimal(newCreditLimitStr))
              response.ok("Credit limit change request enqueued!")

            case None =>
              response.badRequest("Source account not found")
          }

        case None =>
          response.badRequest("User not found")
      }
    }
  }

}