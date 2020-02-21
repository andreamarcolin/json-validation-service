package com.snowplowanalytics.jvs

import zio.{RIO, ZIO}

package object config extends Config.Service[Config] {

  override def root: RIO[Config, Config.Root] = ZIO.accessM(_.config.root)
  override def app: RIO[Config, Config.App]   = ZIO.accessM(_.config.app)
  override def db: RIO[Config, Config.Db]     = ZIO.accessM(_.config.db)

}
