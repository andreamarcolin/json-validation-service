package com.andreamarcolin.jvs.model

sealed trait Status
object Status {
  case object UP   extends Status
  case object DOWN extends Status
}
