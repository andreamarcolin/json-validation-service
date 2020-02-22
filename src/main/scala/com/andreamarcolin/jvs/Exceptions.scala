package com.andreamarcolin.jvs

object Exceptions {
  case object ResourceNotFoundException           extends Throwable
  case object ConflictException                   extends Throwable
  case class ValidationException(message: String) extends Throwable
}
