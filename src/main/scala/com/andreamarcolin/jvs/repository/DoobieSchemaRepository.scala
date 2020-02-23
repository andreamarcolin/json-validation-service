package com.andreamarcolin.jvs.repository

import com.andreamarcolin.jvs.repository.DoobieSchemaRepository.SQL
import com.andreamarcolin.jvs.Exceptions.{ConflictException, ResourceNotFoundException}
import doobie.{Query0, Update0}
import doobie.implicits._
import doobie.postgres._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import io.circe.Json
import zio.{RIO, Task}
import zio.interop.catz._

final class DoobieSchemaRepository(xa: Transactor[Task]) extends SchemaRepository.Service[Any] { self =>

  override def getSchema(schemaId: String): RIO[Any, Json] =
    SQL
      .get(schemaId)
      .option
      .transact(xa)
      .map(_.toRight(ResourceNotFoundException))
      .absolve

  override def saveSchema(schemaId: String, schema: Json): RIO[Any, Int] =
    SQL
      .create(schemaId, schema)
      .withUniqueGeneratedKeys[Int]("id")
      .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => ConflictException }
      .transact(xa)
      .absolve
}

object DoobieSchemaRepository {
  object SQL {
    def get(schemaId: String)(implicit h: LogHandler = LogHandler.nop): Query0[Json] =
      sql"""
        SELECT schema
        FROM json_schema
        WHERE schema_id = $schemaId
      """.query[Json]

    def create(schemaId: String, schema: Json)(implicit h: LogHandler = LogHandler.nop): Update0 =
      sql"""
        INSERT INTO json_schema (schema_id, schema)
        VALUES ($schemaId, $schema)
      """.update
  }
}
