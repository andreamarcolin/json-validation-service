package com.snowplowanalytics.jvs

import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
import io.circe.Encoder

package object model {

  implicit val statusEncoder: Encoder[Status]     = deriveEnumerationEncoder[Status]
  implicit val appErrorEncoder: Encoder[AppError] = Encoder.forProduct1("message")(_.message)

}
