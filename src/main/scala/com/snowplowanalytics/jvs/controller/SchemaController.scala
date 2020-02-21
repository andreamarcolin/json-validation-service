package com.snowplowanalytics.jvs.controller

import cats.implicits._
import com.snowplowanalytics.jvs.Main.AppTask
import com.snowplowanalytics.jvs.model.AppResponse
import com.snowplowanalytics.jvs.model.AppResponse._
import com.snowplowanalytics.jvs.repository.schemaRepository._
import io.circe.generic.auto._
import io.circe.Json
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._

object SchemaController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root / schemaId =>
      for {
        schema   <- getSchema(schemaId)
        response <- Ok(schema)
      } yield response

    case req @ POST -> Root / schemaId =>
      req.decode[Json] { jsonSchema =>
        for {
          _        <- saveSchema(schemaId, jsonSchema)
          response <- Created(AppResponse(Action.uploadSchema, schemaId, ActionStatus.success, none))
        } yield response
      }
  }
}
