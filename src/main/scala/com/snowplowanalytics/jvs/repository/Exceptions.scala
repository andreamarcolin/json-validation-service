package com.snowplowanalytics.jvs.repository

object Exceptions {
  case object ResourceNotFoundException extends Throwable
  case object ConflictException         extends Throwable
}
