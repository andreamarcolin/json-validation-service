package com.andreamarcolin.jvs

import com.andreamarcolin.jvs.model.AppResponse.{Action, ActionStatus}
import io.circe.generic.extras.semiauto.deriveEnumerationEncoder
import io.circe.Encoder

package object model {
  implicit val statusEncoder: Encoder[Status]             = deriveEnumerationEncoder[Status]
  implicit val actionEncoder: Encoder[Action]             = deriveEnumerationEncoder[Action]
  implicit val actionStatusEncoder: Encoder[ActionStatus] = deriveEnumerationEncoder[ActionStatus]
}
