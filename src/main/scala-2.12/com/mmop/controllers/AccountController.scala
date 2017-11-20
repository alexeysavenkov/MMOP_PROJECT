package com.mmop.controllers

import javax.inject.Inject

import com.mmop.db.models.{Accounts, User}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.json4s.JsonAST.JArray
import org.json4s.jackson.JsonMethods._

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

}