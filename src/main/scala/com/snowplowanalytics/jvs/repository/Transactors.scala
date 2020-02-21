package com.snowplowanalytics.jvs.repository

import com.snowplowanalytics.jvs.config.Config.Db
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.fromDriverManager
import zio.Task
import zio.interop.catz._

object Transactors {
  def simpleTransactor(cfg: Db): Transactor[Task] =
    fromDriverManager[Task](cfg.driver, cfg.url, cfg.user, cfg.password)
}
