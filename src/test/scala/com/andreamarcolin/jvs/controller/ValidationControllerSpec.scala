package com.andreamarcolin.jvs.controller

import com.andreamarcolin.jvs.controller.ValidationControllerSpecUtil._
import com.andreamarcolin.jvs.Exceptions.ValidationException
import com.andreamarcolin.jvs.controller.ValidationController._
import com.andreamarcolin.jvs.Fixtures
import com.andreamarcolin.jvs.Fixtures._
import com.andreamarcolin.jvs.Main.AppTask
import io.circe.literal._
import org.http4s.{Response, Status}
import zio.ZIO
import zio.test.{suite, _}
import zio.test.Assertion._

object ValidationControllerSpec
    extends DefaultRunnableSpec(
      suite("ValidationController spec")(
        suite("JSON validation")(
          suite("Empty schema")(
            testM("Empty JSON validates against empty schema") {
              assertM(validate(json"{}", schema.emptySchema), isUnit)
            },
            testM("JSON number validates against empty schema") {
              assertM(validate(json"42", schema.emptySchema), isUnit)
            },
            testM("JSON string validates against empty schema") {
              assertM(validate(json""""hello"""", schema.emptySchema), isUnit)
            },
            testM("JSON boolean validates against empty schema") {
              assertM(validate(json"true", schema.emptySchema), isUnit)
            },
            testM("JSON array validates against empty schema") {
              assertM(validate(json"[42, 24]", schema.emptySchema), isUnit)
            },
            testM("Simple JSON object validates against empty schema") {
              assertM(validate(json"""{ "n": 42, "a": []}""", schema.emptySchema), isUnit)
            }
          ),
          suite("Sample schema with required, non-required and nested properties")(
            testM("Empty JSON does not validate against sample schema") {
              assertM(
                validate(json"{}", schema.sampleSchema).run,
                fails(isSubtype[ValidationException](hasField("message", _.message, isNonEmptyString)))
              )
            },
            testM("Inorrect JSON does not validate against sample schema") {
              assertM(
                validate(json.sampleInvalidJson, schema.sampleSchema).run,
                fails(isSubtype[ValidationException](hasField("message", _.message, isNonEmptyString)))
              )
            },
            testM("Correct JSON validates against sample schema") {
              assertM(validate(json.sampleValidJson, schema.sampleSchema), isUnit)
            },
            testM("Correct JSON with null values validates against sample schema") {
              assertM(validate(json.sampleValidJsonWithNullValues, schema.sampleSchema), isUnit)
            }
          )
        ),
        suite("Request body parsing and schema retrieval")(
          testM("Validating correct JSON against schema produces Ok response") {
            testHandleValidation("sample", json.sampleValidJsonWithNullValues.spaces2)(Status.Ok)
          },
          testM("Validating incorrect JSON against schema produces Ok response with error message") {
            testHandleValidation("sample", json.sampleInvalidJson.spaces2)(Status.Ok)
          },
          testM("Validating correct JSON against non-existent schema produces Not Found response") {
            testHandleValidation("what", json.sampleValidJsonWithNullValues.spaces2)(Status.NotFound)
          },
          testM("Trying to validate syntactically invalid JSON produces Bad Request response") {
            testHandleValidation("sample", "{")(Status.BadRequest)
          },
          testM("Trying to validate empty request payload produces Bad Request response") {
            testHandleValidation("sample", "")(Status.BadRequest)
          }
        )
      )
    )

object ValidationControllerSpecUtil {

  def testHandleValidation(schemaId: String, json: String)(expectedStatus: Status): ZIO[Any, Nothing, TestResult] =
    for {
      env      <- Fixtures.testEnv
      response <- handleValidation(schemaId)(json).provide(env)
    } yield
      assert(
        response,
        hasField("status", (r: Response[AppTask]) => r.status.code, equalTo(expectedStatus.code))
      )

}
