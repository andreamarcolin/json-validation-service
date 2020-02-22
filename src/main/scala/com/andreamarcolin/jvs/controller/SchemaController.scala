package com.andreamarcolin.jvs.controller

import cats.implicits._
import com.andreamarcolin.jvs._
import com.andreamarcolin.jvs.Main.AppTask
import com.andreamarcolin.jvs.model.AppResponse._
import com.andreamarcolin.jvs.model.AppResponse.Action._
import com.andreamarcolin.jvs.model.AppResponse.ActionStatus._
import com.andreamarcolin.jvs.repository.schemaRepository._
import com.andreamarcolin.jvs.Exceptions.{ConflictException, ResourceNotFoundException}
import io.circe.parser.parse
import io.circe.ParsingFailure
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.ZIO

object SchemaController extends Http4sDsl[AppTask] {
  def routes: HttpRoutes[AppTask] = HttpRoutes.of[AppTask] {
    case GET -> Root / schemaId        => handleGet(schemaId)
    case req @ POST -> Root / schemaId => req.decode[String](handlePost(schemaId))
  }

  def handleGet(schemaId: String): AppTask[Response[AppTask]] = {
    val notFound =
      httpResponse(NotFound, downloadSchema, schemaId, error, "JSON Schema not found".some)
    val internalServerError =
      httpResponse(InternalServerError, downloadSchema, schemaId, error, "Oops! Something went wrong".some)

    getSchema(schemaId)
      .flatMap(Ok(_))
      .catchAll {
        case ResourceNotFoundException => notFound
        case _                         => internalServerError
      }
  }

  def handlePost(schemaId: String)(requestBody: String): AppTask[Response[AppTask]] = {
    val created =
      httpResponse(Created, uploadSchema, schemaId, ActionStatus.success)
    val badRequest =
      httpResponse(BadRequest, uploadSchema, schemaId, error, "Invalid JSON".some)
    val conflict =
      httpResponse(Conflict, uploadSchema, schemaId, error, "A JSON Schema with this ID already exists".some)
    val internalServerError =
      httpResponse(InternalServerError, uploadSchema, schemaId, error, "Oops! Something went wrong".some)

    ZIO
      .fromEither(parse(requestBody))
      .flatMap(
        saveSchema(schemaId, _)
          .zipRight(created)
          .catchAll {
            case ConflictException => conflict
            case _                 => internalServerError
          }
      )
      .catchAll {
        case ParsingFailure(_, _) => badRequest
        case _                    => internalServerError
      }
  }

}
