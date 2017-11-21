package com.mmop

import com.mmop.controllers._
import com.mmop.db.models.AutomaticTransaction
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}

object Server extends HttpServer {
//  override val modules = Seq(
//    DoEverythingModule)

  override def configureHttp(router: HttpRouter): Unit = {
    router.
      add[AuthController].add[AccountController].add[TransactionsController].add[AutomaticTransactionsController]
  }

  AutomaticTransactionBackgroundThread.init()
//  val server = Http.serve(":8080", service)
//  Await.ready(server)
}
