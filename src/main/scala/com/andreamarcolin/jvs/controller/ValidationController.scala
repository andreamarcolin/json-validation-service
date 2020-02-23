package com.andreamarcolin.jvs.controller

import cats.implicits._
import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.model.AppResponse._
import com.andreamarcolin.jvs.model.AppResponse.Action._
import com.andreamarcolin.jvs.model.AppResponse.ActionStatus._
import com.andreamarcolin.jvs.repository.schemaRepository._
import com.andreamarcolin.jvs.Exceptions.{ResourceNotFoundException, ValidationException}
import io.circe.ParsingFailure
import io.circe.parser.parse
import io.circe.schema.Schema
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.ZIO
import zio.logging._
import zio.interop.catz._
import zio.logging.LogLevel._

object ValidationController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case req @ POST -> Root / schemaId => req.decode[String](handleValidation(schemaId))
  }

  def handleValidation(schemaId: String)(requestBody: String): AppTask[Response[AppTask]] = {
    val valid =
      httpResponse(Ok, validateDocument, schemaId, success)
    val invalid: String => AppTask[Response[AppTask]] =
      msg => httpResponse(Ok, validateDocument, schemaId, error, msg.some)
    val notFound =
      httpResponse(NotFound, validateDocument, schemaId, error, "JSON Schema not found".some)
    val badRequest =
      httpResponse(BadRequest, validateDocument, schemaId, error, "Invalid JSON".some)
    val internalServerError =
      httpResponse(InternalServerError, validateDocument, schemaId, error, "Oops! Something went wrong".some)

    ZIO
      .fromEither(parse(requestBody))
      .flatMap(
        json =>
          getSchema(schemaId)
            .flatMap(
              schema =>
                ZIO
                  .fromEither(Schema.load(schema).validate(json.deepDropNullValues).toEither)
                  .mapError(errors => ValidationException(errors.toList.map(_.getMessage).mkString("; ")))
                  .zipRight(valid)
                  .catchAll {
                    case ValidationException(msg) => invalid(msg)
                  }
            )
            .catchAll {
              case ResourceNotFoundException => log(Debug)(s"JSON Schema with id '$schemaId' not found") *> notFound
              case ex                        => logThrowable(ex) *> internalServerError
            }
      )
      .catchAll {
        case ParsingFailure(msg, _) => log(Debug)(s"Invalid JSON: $msg") *> badRequest
        case ex                     => logThrowable(ex) *> internalServerError
      }
  }
}
