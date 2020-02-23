package com.andreamarcolin.jvs.controller

import cats.implicits._
import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.model.AppResponse._
import com.andreamarcolin.jvs.model.AppResponse.Action._
import com.andreamarcolin.jvs.model.AppResponse.ActionStatus._
import com.andreamarcolin.jvs.repository.schemaRepository._
import com.andreamarcolin.jvs.Exceptions.{ResourceNotFoundException, ValidationException}
import com.andreamarcolin.jvs.repository.SchemaRepository
import io.circe.{Json, ParsingFailure}
import io.circe.parser.parse
import io.circe.schema.Schema
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.{Task, UIO, URIO, ZIO}
import zio.interop.catz._
import zio.logging._
import zio.logging.LogLevel._

object ValidationController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case req @ POST -> Root / schemaId => req.decode[String](handleValidation(schemaId))
  }

  def handleValidation(schemaId: String)(requestBody: String): URIO[SchemaRepository with Logging, Response[AppTask]] = {
    val validationOk =
      httpResponse(Ok, validateDocument, schemaId, success)
    val validationError: String => Response[AppTask] =
      msg => httpResponse(Ok, validateDocument, schemaId, error, msg.some)
    val notFound =
      httpResponse(NotFound, validateDocument, schemaId, error, "JSON Schema not found".some)
    val badRequest =
      httpResponse(BadRequest, validateDocument, schemaId, error, "Invalid JSON".some)
    val internalServerError =
      httpResponse(InternalServerError, validateDocument, schemaId, error, "Oops! Something went wrong".some)

    (for {
      json   <- ZIO.fromEither(parse(requestBody))
      schema <- getSchema(schemaId)
      _      <- validate(json, schema)
    } yield validationOk).catchAll {
      case ValidationException(msg)  => UIO.succeed(validationError(msg))
      case ResourceNotFoundException => log(Debug)(s"JSON Schema with id '$schemaId' not found").as(notFound)
      case ParsingFailure(msg, _)    => log(Debug)(s"Invalid JSON: $msg").as(badRequest)
      case ex                        => logThrowable(ex).as(internalServerError)
    }
  }

  def validate(json: Json, schema: Json): Task[Unit] =
    ZIO
      .fromEither(Schema.load(schema).validate(json.deepDropNullValues).toEither)
      .mapError(errors => ValidationException(errors.toList.map(_.getMessage).mkString("; ")))
}
