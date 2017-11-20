package com.mmop.controllers

import javax.inject.Inject

import com.mmop.db.models.{Accounts, AutomaticTransactions, Transactions, User}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods.{pretty, render}

import scala.util.Try

class AutomaticTransactionsController @Inject()(
                                        //exampleService: ExampleService
                                      ) extends Controller {

  post("/transactions/automatic/:src/:dest") { request: Request =>
    val srcAccountId = request.getIntParam("src")
    val destAccountId = request.getIntParam("dest")
    val amountStr = request.getParam("amount")
    val token = request.getParam("token")
    val intervalDays = request.getIntParam("intervalDays")

    if(Seq(token).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Not all parameters specified")
    } else if(Try { assert( (amountStr.toDouble % 0.01) > 0) } isFailure) {
      response.badRequest("Bad amount parameter")
    } else {
      // These nested expressions can be avoided by for-expression
      User.byPublicToken(token) match {
        case Some(user) =>
          Accounts.byId(srcAccountId) match {
            case Some(srcAccount) if srcAccount.userId != user.id =>
              response.badRequest("You don't own source account")

            case Some(srcAccount) =>
              Accounts.byId(destAccountId) match {
                case Some(destAccount) =>
                  AutomaticTransactions.create(srcAccount, destAccount, amountStr.toDouble, intervalDays) match {
                    case Left(error) => response.badRequest(error)
                    case Right(_) =>
                      val allTransactions = AutomaticTransactions.byAccount(srcAccount)
                      val output = JArray(allTransactions.map(_.asOutput).toList)
                      response.ok(pretty(render(output)))
                  }

                case None =>
                  response.badRequest("Destination account not found")
              }
            case None =>
              response.badRequest("Source account not found")
          }

        case None =>
          response.badRequest("User not found")
      }
    }
  }

  delete("/transactions/automatic/:id") { request: Request =>
    val transactionId = request.getIntParam("id")
    val token = request.getParam("token")

    User.byPublicToken(token) match {
      case Some(user) =>
        AutomaticTransactions.byId(transactionId) match {
          case Some(transaction) =>
            Accounts.byId(transaction.sourceAccount) match {
              case Some(srcAccount) if srcAccount.userId != user.id =>
                response.badRequest("Account does not belong to you")

              case Some(srcAccount) =>
                transaction.delete()

                val allTransactions = AutomaticTransactions.byAccount(srcAccount)
                val output = JArray(allTransactions.map(_.asOutput).toList)
                response.ok(pretty(render(output)))
              case None =>
                response.badRequest("Account does not exist")
            }

          case None =>
            response.badRequest("Account not found")
        }

      case None =>
        response.badRequest("User not found")
    }

  }

  get("/transactions/automatic") { request: Request =>
    val accountId = request.getIntParam("account")
    val token = request.getParam("token")

    User.byPublicToken(token) match {
      case Some(user) =>
        Accounts.byId(accountId) match {
          case Some(srcAccount) if srcAccount.userId != user.id =>
            response.badRequest("Account does not belong to you")

          case Some(srcAccount) =>

            val allTransactions = AutomaticTransactions.byAccount(srcAccount)
            val output = JArray(allTransactions.map(_.asOutput).toList)
            response.ok(pretty(render(output)))

          case None =>
            response.badRequest("Account does not exist")
        }

      case None =>
        response.badRequest("User not found")
    }

  }



}