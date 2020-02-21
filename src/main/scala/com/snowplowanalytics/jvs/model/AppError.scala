package com.snowplowanalytics.jvs.model

sealed trait AppError { def message: String }
object AppError {
  case object ResourceNotFound             extends AppError { def message = "Resource not found" }
  case object InternalServerError          extends AppError { def message = "Internal Server Error" }
  case class GenericError(message: String) extends AppError

  // Widen output type for easier discovery of encoders/decoders at call place
  val resourceNotFound: AppError          = ResourceNotFound
  val internalServerError: AppError       = InternalServerError
  def genericError(msg: String): AppError = GenericError(msg)
}
