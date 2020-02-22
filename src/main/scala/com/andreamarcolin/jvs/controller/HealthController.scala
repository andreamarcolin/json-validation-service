package com.andreamarcolin.jvs.controller

import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.config
import com.andreamarcolin.jvs.config.Config
import com.andreamarcolin.jvs.model.{AppStatus, Status}
import com.andreamarcolin.jvs.repository.Transactors
import doobie.implicits._
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import zio.RIO
import zio.interop.catz._

object HealthController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root =>
      for {
        dbStatus <- checkDbStatus
        response <- Ok(AppStatus(dbStatus))
      } yield response
  }

  private def checkDbStatus: RIO[Config, Status] =
    for {
      conf   <- config.db
      status <- sql"SELECT 1".query[Int].unique.map(_ => Status.UP).transact(Transactors.simpleTransactor(conf))
    } yield status
}
