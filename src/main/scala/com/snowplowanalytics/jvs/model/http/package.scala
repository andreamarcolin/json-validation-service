package com.snowplowanalytics.jvs.model

import com.snowplowanalytics.jvs.model.http.AppResponse._
import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
import io.circe.Encoder

package object http {
  implicit val statusEncoder: Encoder[Status]             = deriveEnumerationEncoder[Status]
  implicit val actionEncoder: Encoder[Action]             = deriveEnumerationEncoder[Action]
  implicit val actionStatusEncoder: Encoder[ActionStatus] = deriveEnumerationEncoder[ActionStatus]
}
