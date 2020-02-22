package com.snowplowanalytics.jvs

import cats.implicits._
import com.snowplowanalytics.jvs.config.Config
import doobie.util.meta.Meta
import io.circe._
import org.postgresql.util.PGobject
import zio.{RIO, ZIO, ZManaged}
import zio.macros.delegate.Mix

package object repository {
  object schemaRepository extends SchemaRepository.Service[SchemaRepository] {
    override def getSchema(schemaId: String): RIO[SchemaRepository, Json] =
      ZIO.accessM(_.schemaRepository.getSchema(schemaId))
    override def saveSchema(schemaId: String, schema: Json): RIO[SchemaRepository, Int] =
      ZIO.accessM(_.schemaRepository.saveSchema(schemaId, schema))
  }

  def withDoobieRepositories[R <: Config](
      implicit ev: R Mix SchemaRepository
  ): ZManaged[R, Throwable, R with SchemaRepository] =
    for {
      cfg        <- config.db.toManaged_
      transactor = Transactors.simpleTransactor(cfg)
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
