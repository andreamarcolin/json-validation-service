package com.andreamarcolin.jvs

import cats.implicits._
import cats.data.Kleisli
import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.controller._
import com.andreamarcolin.jvs.model.AppResponse._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.middleware.{AutoSlash, CORS}
import org.http4s.server.Router
import org.http4s.Status.NotFound
import zio.interop.catz._

object Web {
  def buildRouter(baseUrl: String): HttpApp[AppTask] = middleware(router(baseUrl))

  private def router(baseUrl: String): HttpRoutes[AppTask] =
    Router[AppTask](
      s"$baseUrl/health" -> HealthController.routes,
      s"$baseUrl/schema" -> SchemaController.routes,
      s"$baseUrl/validate" -> ValidationController.routes
    )

  private def middleware: HttpRoutes[AppTask] => HttpApp[AppTask] =
    ((http: HttpRoutes[AppTask]) => AutoSlash(http)) andThen
      ((http: HttpRoutes[AppTask]) => CORS(http)) andThen
      ((http: HttpRoutes[AppTask]) => orNotFound(http))

  private def orNotFound[A](routes: HttpRoutes[AppTask]): HttpApp[AppTask] =
    Kleisli(a => routes.run(a).getOrElse(buildHttpResponse(NotFound, "Resource not found".some)))
}
