package com.andreamarcolin.jvs.controller

import com.andreamarcolin.jvs.controller.ValidationControllerSpecUtil._
import com.andreamarcolin.jvs.Exceptions.{ResourceNotFoundException, ValidationException}
import com.andreamarcolin.jvs.controller.ValidationController._
import com.andreamarcolin.jvs.repository.SchemaRepository
import com.andreamarcolin.jvs.Main.AppTask
import io.circe.literal._
import io.circe.Json
import org.http4s.{Response, Status}
import zio.{RIO, Ref, UIO, ZIO}
import zio.logging.{LogAnnotation, Logger, Logging}
import zio.logging.LogLevel._
import zio.test.{suite, _}
import zio.test.Assertion._

object ValidationControllerSpec
    extends DefaultRunnableSpec(
      suite("ValidationController spec")(
        suite("JSON validation")(
          suite("Empty schema")(
            testM("Empty JSON validates against empty schema") {
              assertM(validate(json"{}", emptySchema), isUnit)
            },
            testM("JSON number validates against empty schema") {
              assertM(validate(json"42", emptySchema), isUnit)
            },
            testM("JSON string validates against empty schema") {
              assertM(validate(json""""hello"""", emptySchema), isUnit)
            },
            testM("JSON boolean validates against empty schema") {
              assertM(validate(json"true", emptySchema), isUnit)
            },
            testM("JSON array validates against empty schema") {
              assertM(validate(json"[42, 24]", emptySchema), isUnit)
            },
            testM("Simple JSON object validates against empty schema") {
              assertM(validate(json"""{ "n": 42, "a": []}""", emptySchema), isUnit)
            }
          ),
          suite("Sample schema with required, non-required and nested properties")(
            testM("Empty JSON does not validate against sample schema") {
              assertM(
                validate(json"{}", sampleSchema).run,
                fails(isSubtype[ValidationException](hasField("message", _.message, isNonEmptyString)))
              )
            },
            testM("Inorrect JSON does not validate against sample schema") {
              assertM(
                validate(sampleInvalidJson, sampleSchema).run,
                fails(isSubtype[ValidationException](hasField("message", _.message, isNonEmptyString)))
              )
            },
            testM("Correct JSON validates against sample schema") {
              assertM(validate(sampleValidJson, sampleSchema), isUnit)
            },
            testM("Correct JSON with null values validates against sample schema") {
              assertM(validate(sampleValidJsonWithNullValues, sampleSchema), isUnit)
            }
          )
        ),
        suite("Request body parsing and schema retrieval")(
          testM("Validating correct JSON against schema produces Ok response") {
            testHandleValidation("sample", sampleValidJsonWithNullValues.spaces2)(Status.Ok)
          },
          testM("Validating incorrect JSON against schema produces Ok response with error message") {
            testHandleValidation("sample", sampleInvalidJson.spaces2)(Status.Ok)
          },
          testM("Validating correct JSON against non-existent schema produces Not Found response") {
            testHandleValidation("what", sampleValidJsonWithNullValues.spaces2)(Status.NotFound)
          },
          testM("Trying to validate syntactically invalid JSON produces Bad Request response") {
            testHandleValidation("sample", "{")(Status.BadRequest)
          },
          testM("Trying to validate empty request payload produces Bad Request response") {
            testHandleValidation("sample", "")(Status.BadRequest)
          },
        )
      )
    )

object ValidationControllerSpecUtil {
  val emptySchema = json"{}"

  val sampleSchema =
    json"""
    {
      "$$schema": "http://json-schema.org/draft-04/schema#",
      "type": "object",
      "properties": {
        "source": {
          "type": "string"
        },
        "destination": {
          "type": "string"
        },
        "timeout": {
          "type": "integer",
          "minimum": 0,
          "maximum": 32767
        },
        "chunks": {
          "type": "object",
          "properties": {
            "size": {
              "type": "integer"
            },
            "number": {
              "type": "integer"
            }
          },
          "required": ["size"]
        }
      },
      "required": ["source", "destination"]
    }
    """

  val sampleValidJson =
    json"""
    {
      "source": "/home/alice/image.iso",
      "destination": "/mnt/storage",
      "chunks": {
        "size": 1024
      }
    }
    """

  val sampleInvalidJson =
    json"""
    {
      "destination": "/mnt/storage",
      "chunks": {}
    }
    """

  val sampleValidJsonWithNullValues =
    json"""
    {
      "source": "/home/alice/image.iso",
      "destination": "/mnt/storage",
      "timeout": null,
      "chunks": {
        "size": 1024,
        "number": null
      }
    }
    """

  val testLogger: UIO[Logger] = Logger.make(
    (ctx, msg) =>
      ctx.get(LogAnnotation.Level).level match {
        case Off.level                                           => ZIO.succeed(())
        case Debug.level | Trace.level | Info.level | Warn.level => ZIO.effectTotal(System.out.println(msg))
        case Error.level | Fatal.level                           => ZIO.effectTotal(System.err.println(msg))
    }
  )

  class TestSchemaRepository(ref: Ref[Int]) extends SchemaRepository.Service[Any] {
    override def getSchema(schemaId: String): RIO[Any, Json] =
      ZIO.fromEither(Either.cond(schemaId == "sample", sampleSchema, ResourceNotFoundException))
    override def saveSchema(schemaId: String, schema: Json): RIO[Any, Int] =
      ref.update(_ + 1)
  }

  val testEnv: UIO[SchemaRepository with Logging] =
    for {
      schemaRepositoryState <- Ref.make(0)
      log                   <- testLogger
    } yield
      new SchemaRepository with Logging {
        override val schemaRepository: SchemaRepository.Service[Any] = new TestSchemaRepository(schemaRepositoryState)
        override def logger: Logger                                  = log
      }

  def testHandleValidation(schemaId: String, json: String)(expectedStatus: Status): ZIO[Any, Nothing, TestResult] =
    for {
      env      <- testEnv
      response <- handleValidation(schemaId)(json).provide(env)
    } yield
      assert(
        response,
        hasField("status", (r: Response[AppTask]) => r.status.code, equalTo(expectedStatus.code))
      )
}
