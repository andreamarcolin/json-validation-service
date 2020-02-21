package com.snowplowanalytics.jvs
import io.circe.Json
import zio.{RIO, ZIO}

package object repository {
  object schemaRepository extends SchemaRepository.Service[SchemaRepository] {
    override def getSchema(schemaId: String): RIO[SchemaRepository, Json] =
      ZIO.accessM(_.schemaRepository.getSchema(schemaId))
    override def saveSchema(schemaId: String, schema: Json): RIO[SchemaRepository, Int] =
      ZIO.accessM(_.schemaRepository.saveSchema(schemaId, schema))
  }
}
