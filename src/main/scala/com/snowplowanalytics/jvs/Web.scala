package com.snowplowanalytics.jvs

import cats.data.Kleisli
import com.snowplowanalytics.jvs.Main.AppTask
import com.snowplowanalytics.jvs.controller._
import com.snowplowanalytics.jvs.model.{appErrorEncoder, AppError}
import com.snowplowanalytics.jvs.model.AppError._
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.server.middleware.{AutoSlash, CORS}
import org.http4s.server.Router
import zio.interop.catz._

object Web {

  def buildRouter(baseUrl: String): HttpApp[AppTask] = middleware(router(baseUrl))

  private def router(baseUrl: String): HttpRoutes[AppTask] =
    Router[AppTask](
      s"$baseUrl/health"     -> HealthController.routes
    )

  private def middleware: HttpRoutes[AppTask] => HttpApp[AppTask] =
    ((http: HttpRoutes[AppTask]) => AutoSlash(http)) andThen
      ((http: HttpRoutes[AppTask]) => CORS(http)) andThen
      ((http: HttpRoutes[AppTask]) => orNotFound(http))


  private def orNotFound[A](routes: HttpRoutes[AppTask]): HttpApp[AppTask] =
    Kleisli(a => routes.run(a).getOrElse(Response.notFound[AppTask].withEntity[AppError](ResourceNotFound)))

}