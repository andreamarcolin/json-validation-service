package com.andreamarcolin.jvs.controller

import cats.implicits._
import com.andreamarcolin.jvs._
import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.model.AppResponse._
import com.andreamarcolin.jvs.model.AppResponse.Action._
import com.andreamarcolin.jvs.model.AppResponse.ActionStatus._
import com.andreamarcolin.jvs.repository.schemaRepository._
import com.andreamarcolin.jvs.Exceptions.{ConflictException, ResourceNotFoundException}
import com.andreamarcolin.jvs.repository.SchemaRepository
import io.circe.parser.parse
import io.circe.ParsingFailure
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.{URIO, ZIO}
import zio.interop.catz._
import zio.logging.{log, logThrowable, Logging}
import zio.logging.LogLevel.Debug

object SchemaController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root / schemaId        => handleGet(schemaId)
    case req @ POST -> Root / schemaId => req.decode[String](handlePost(schemaId))
  }

  def handleGet(schemaId: String): URIO[SchemaRepository with Logging, Response[AppTask]] = {
    val notFound =
      httpResponse(NotFound, downloadSchema, schemaId, error, "JSON Schema not found".some)
    val internalServerError =
      httpResponse(InternalServerError, downloadSchema, schemaId, error, "Oops! Something went wrong".some)

    (for {
      schema <- getSchema(schemaId)
      ok     = Response[AppTask](Ok).withEntity(schema)
    } yield ok).catchAll {
      case ResourceNotFoundException => log(Debug)(s"JSON Schema with id '$schemaId' not found").as(notFound)
      case ex                        => logThrowable(ex).as(internalServerError)
    }
  }

  def handlePost(schemaId: String)(requestBody: String): URIO[SchemaRepository with Logging, Response[AppTask]] = {
    val created =
      httpResponse(Created, uploadSchema, schemaId, ActionStatus.success)
    val conflict =
      httpResponse(Conflict, uploadSchema, schemaId, error, "A JSON Schema with this ID already exists".some)
    val badRequest =
      httpResponse(BadRequest, uploadSchema, schemaId, error, "Invalid JSON".some)
    val internalServerError =
      httpResponse(InternalServerError, uploadSchema, schemaId, error, "Oops! Something went wrong".some)

    (for {
      schema <- ZIO.fromEither(parse(requestBody))
      _      <- saveSchema(schemaId, schema)
    } yield created).catchAll {
      case ConflictException      => log(Debug)(s"JSON Schema with id $schemaId already exists").as(conflict)
      case ParsingFailure(msg, _) => log(Debug)(s"Invalid JSON: $msg").as(badRequest)
      case ex                     => logThrowable(ex).as(internalServerError)
    }
  }
}
