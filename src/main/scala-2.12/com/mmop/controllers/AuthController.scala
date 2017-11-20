package com.mmop.controllers

import java.util.Date
import javax.inject.Inject

import com.mmop.SmsHandler
import com.mmop.db.models.User
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class AuthController @Inject()(
                                     //exampleService: ExampleService
                                 ) extends Controller {

  get("/auth/send-sms") { request: Request =>
    val phoneNumber = Option(request.getParam("phone")).map(_.replace(' ', '+')).orNull
    val password = request.getParam("password")
    if(Seq(phoneNumber, password).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Phone number or password is empty")
    } else {
      User.byPhoneAndPassword(phoneNumber, password) match {
        case Some(user) =>
          val timeSmsSent = user.timeSmsSent
          val msSinceLastSmsSent = new Date().getTime - Option(timeSmsSent).map(_.getTime).getOrElse(0L)
          if(msSinceLastSmsSent > 1000 * 60 * 5) {
            val smsCode = user.generateNewSmsCode()
            SmsHandler.sendLoginCode(phoneNumber, smsCode)
            response.ok("Sms sent!")
          } else {
            val r = response.status(429)
            val timeoutMinutes = 5 - msSinceLastSmsSent / (1000 * 60)
            r.setContentString(s"""{"timeoutMinutes": ${timeoutMinutes}}""")
            r
          }

        case None =>
          response.badRequest("User not found")
      }
    }
  }

  get("/auth/verify-sms") { request: Request =>
    val phoneNumber = Option(request.getParam("phone")).map(_.replace(' ', '+')).orNull
    val code = request.getParam("code")
    if(Seq(phoneNumber, code).exists(x => x == null || x.isEmpty)) {
      response.badRequest("Phone number or code is empty")
    } else {
      User.byPhone(phoneNumber) match {
        case Some(user) =>
          if(user.smsCode == code) {
            user.clearSmsCode()
            response.ok(s"""{"token":"${user.publicToken}"}""")
          } else {
            user.clearSmsCode()
            response.unauthorized("Unauthorized")
          }
        case None =>
          response.badRequest("User not found")
      }
    }
  }


}