package com.andreamarcolin.jvs

import com.andreamarcolin.jvs.repository.SchemaRepository
import com.andreamarcolin.jvs.Exceptions.{ConflictException, ResourceNotFoundException}
import io.circe.Json
import io.circe.literal._
import zio.logging.{LogAnnotation, Logger, Logging}
import zio.logging.LogLevel.{Debug, Error, Fatal, Info, Off, Trace, Warn}
import zio.{Ref, RIO, UIO, ZIO}

object Fixtures {
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
      if (schemaId == "sample") ZIO.succeed(schema.sampleSchema) else ZIO.fail(ResourceNotFoundException)
    override def saveSchema(schemaId: String, schema: Json): RIO[Any, Int] =
      if (schemaId == "sample") ZIO.fail(ConflictException) else ref.update(_ + 1)
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

  object json {
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
  }

  object schema {
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
  }
}
