package com.andreamarcolin.jvs

import cats.implicits._
import com.andreamarcolin.jvs.config.Config
import com.andreamarcolin.jvs.repository.Transactors._
import doobie.util.meta.Meta
import io.circe._
import org.postgresql.util.PGobject
import zio.{RIO, ZIO, ZManaged}
import zio.blocking.Blocking
import zio.macros.delegate.Mix

package object repository {
  object schemaRepository extends SchemaRepository.Service[SchemaRepository] {
    override def getSchema(schemaId: String): RIO[SchemaRepository, Json] =
      ZIO.accessM(_.schemaRepository.getSchema(schemaId))
    override def saveSchema(schemaId: String, schema: Json): RIO[SchemaRepository, Int] =
      ZIO.accessM(_.schemaRepository.saveSchema(schemaId, schema))
  }

  def withDoobieRepositories[R <: Blocking with Config](
      implicit ev: R Mix SchemaRepository
  ): ZManaged[R, Throwable, R with SchemaRepository] =
    for {
      cfg        <- config.db.toManaged_
      blockingEC <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor.map(_.asEC)).toManaged_
      mainEC     <- ZIO.runtime[Blocking].map(_.platform.executor.asEC).toManaged_
      transactor <- pooledTransactor(cfg, mainEC, blockingEC)
      repos = new SchemaRepository {
        val schemaRepository = new DoobieSchemaRepository(transactor)
      }
      r <- ZManaged.environment[R].map(ev.mix(_, repos))
    } yield r

  implicit val JsonbMeta: doobie.Meta[Json] = Meta.Advanced
    .other[PGobject]("jsonb")
    .timap[Json](
      jsonStr => parser.parse(jsonStr.getValue).leftMap[Json](err => throw err).merge
    )(
      json => {
        val o = new PGobject
        o.setType("jsonb")
        o.setValue(json.noSpaces)
        o
      }
    )
}
