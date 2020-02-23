package com.andreamarcolin.jvs.repository

import cats.effect.Blocker
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import com.andreamarcolin.jvs.config.Config
import com.andreamarcolin.jvs.config.Config.Db
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.fromDriverManager
import zio.{Managed, Task, ZIO}
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object Transactors {
  def pooledTransactor(
      cfg: Config.Db,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, Transactor[Task]] =
    ZIO.runtime[Any].toManaged_.flatMap { implicit rt =>
      HikariTransactor
        .fromHikariConfig[Task](
          hikariConfig(cfg),
          connectEC,
          Blocker.liftExecutionContext(transactEC)
        )
        .toManaged
    }

  private[this] def hikariConfig(conf: Config.Db): HikariConfig = {
    val hikariConf = new HikariConfig()
    hikariConf.setJdbcUrl(conf.url)
    hikariConf.setConnectionTestQuery("SELECT 1")
    hikariConf.setDriverClassName(conf.driver)
    hikariConf.setUsername(conf.user)
    hikariConf.setPassword(conf.password)
    hikariConf.setPoolName("Hikari CP")
    hikariConf.setMinimumIdle(1)
    hikariConf.setMaximumPoolSize(10)
    hikariConf
  }

  def simpleTransactor(cfg: Db): Transactor[Task] =
    fromDriverManager[Task](cfg.driver, cfg.url, cfg.user, cfg.password)
}
