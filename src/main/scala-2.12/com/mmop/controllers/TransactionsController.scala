package com.mmop.controllers

import javax.inject.Inject

import com.mmop.db.models.{Accounts, Transactions, User}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods._

import scala.util.Try

class TransactionsController @Inject()(
                                        //exampleService: ExampleService
                                      ) extends Controller {

  post("/transactions/create/:src/:dest") { request: Request =>
    val srcAccountId = request.getIntParam("src")
    val destAccountId = request.getIntParam("dest")
    val amountStr = request.getParam("amount")
    val token = request.getParam("token")

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
                  Transactions.create(srcAccount, destAccount, amountStr.toDouble) match {
                    case Left(error) => response.badRequest(error)
                    case Right(_) => response.ok("Transaction successful")
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

  get("/transactions") { request: Request =>
    val accountId = request.getIntParam("account")
    val token = request.getParam("token")

    User.byPublicToken(token) match {
      case Some(user) =>
        Accounts.byId(accountId) match {
          case Some(account) =>
            val transactions = Transactions.byAccount(account)

            val output = JArray(
              transactions.map(transaction => {
                val isIncoming: Boolean = transaction.destinationAccount == accountId
                JObject(
                  "id" -> JInt(transaction.id),
                  "sourceAccount" -> JInt(if(isIncoming) transaction.sourceAccount else transaction.destinationAccount),
                  "amount" -> JDecimal(if(isIncoming) transaction.amount else -transaction.amount),
                  "timeProcessed" -> JString(transaction.timeCommitted.toString)
                )
              }).toList
            )

            response.ok(pretty(render(output)))
          case None =>
            response.badRequest("Account not found")
        }

      case None =>
        response.badRequest("User not found")
    }

  }



}
