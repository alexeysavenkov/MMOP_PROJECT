package com.mmop

import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}

object Server extends HttpServer {
//  override val modules = Seq(
//    DoEverythingModule)

  override def configureHttp(router: HttpRouter): Unit = {
    router.
      add[AuthController]
  }
//  val server = Http.serve(":8080", service)
//  Await.ready(server)
}
