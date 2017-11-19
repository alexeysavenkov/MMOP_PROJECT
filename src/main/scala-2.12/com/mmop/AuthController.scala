package com.mmop

import javax.inject.Inject

import com.mmop.db.User
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
          val smsCode = user.generateNewSmsCode()
          SmsHandler.sendLoginCode(phoneNumber, smsCode)
        case None =>
          response.badRequest("User not found")
      }
    }
  }

  get("/auth/verify-sms") { request: Request =>
    "pong"
  }


}