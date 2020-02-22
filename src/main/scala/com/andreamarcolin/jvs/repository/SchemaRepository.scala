package com.andreamarcolin.jvs.repository

import io.circe.Json
import zio.RIO

trait SchemaRepository {
  val schemaRepository: SchemaRepository.Service[Any]
}

object SchemaRepository {
  trait Service[R] {
    def getSchema(schemaId: String): RIO[R, Json]
    def saveSchema(schemaId: String, schema: Json): RIO[R, Int]
  }
}
