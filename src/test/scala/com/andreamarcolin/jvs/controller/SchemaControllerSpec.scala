package com.andreamarcolin.jvs.controller

import com.andreamarcolin.jvs.controller.SchemaController._
import com.andreamarcolin.jvs.controller.SchemaControllerSpecUtil._
import com.andreamarcolin.jvs.Fixtures
import com.andreamarcolin.jvs.Fixtures.schema
import com.andreamarcolin.jvs.Main.AppTask
import org.http4s.{Response, Status}
import zio.test._
import zio.test.Assertion.{equalTo, hasField}
import zio.ZIO

object SchemaControllerSpec
    extends DefaultRunnableSpec(
      suite("SchemaController spec")(
        suite("Schema download")(
          testM("Getting existent schema produces Ok response") {
            testHandleGet("sample")(Status.Ok)
          },
          testM("Trying to get non-existent schema produces Not Found response") {
            testHandleGet("what")(Status.NotFound)
          }
        ),
        suite("Schema upload")(
          testM("Uploading valid JSON Schema produces Created response") {
            testHandlePost("test", schema.sampleSchema.spaces2)(Status.Created)
          },
          testM("Trying to upload valid JSON Schema with ID of already existing one produces Conflict response") {
            testHandlePost("sample", schema.sampleSchema.spaces2)(Status.Conflict)
          },
          testM("Trying to upload syntactically invalid JSON produces Bad Request response") {
            testHandlePost("test", "{")(Status.BadRequest)
          },
          testM("Empty payload request produces Bad Request response") {
            testHandlePost("test", "")(Status.BadRequest)
          }
        )
      )
    )

object SchemaControllerSpecUtil {

  def testHandleGet(schemaId: String)(expectedStatus: Status): ZIO[Any, Nothing, TestResult] =
    for {
      env      <- Fixtures.testEnv
      response <- handleGet(schemaId).provide(env)
    } yield
      assert(
        response,
        hasField("status", (r: Response[AppTask]) => r.status.code, equalTo(expectedStatus.code))
      )

  def testHandlePost(schemaId: String, schema: String)(expectedStatus: Status): ZIO[Any, Nothing, TestResult] =
    for {
      env      <- Fixtures.testEnv
      response <- handlePost(schemaId)(schema).provide(env)
    } yield
      assert(
        response,
        hasField("status", (r: Response[AppTask]) => r.status.code, equalTo(expectedStatus.code))
      )

}