package com.snowplowanalytics.jvs

import com.snowplowanalytics.jvs.model.AppResponse._
import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
import io.circe.Encoder

package object model {
  implicit val statusEncoder: Encoder[Status]     = deriveEnumerationEncoder[Status]
  implicit val appErrorEncoder: Encoder[AppError] = Encoder.forProduct1("message")(_.message)

  implicit val actionEncoder: Encoder[Action]             = deriveEnumerationEncoder[Action]
  implicit val actionStatusEncoder: Encoder[ActionStatus] = deriveEnumerationEncoder[ActionStatus]
}
