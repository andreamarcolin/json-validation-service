package com.snowplowanalytics.jvs.model.http

sealed trait Status
object Status {
  case object UP   extends Status
  case object DOWN extends Status
}
