package com.snowplowanalytics.jvs.config

import com.snowplowanalytics.jvs.config.Config.Root
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import zio.{Task, UIO, ZIO, ZManaged}
import zio.macros.delegate.Mix

final class PureconfigConfigService extends Config.Service[Any] {
  import pureconfig.generic.auto._ // WARN: do not remove. IntelliJ false positive "unused import"

  private val parseConf: UIO[Root] = ZIO
    .fromEither(ConfigSource.default.load[Root])
    .mapError(ConfigReaderException(_))
    .orDie // Exit if we can't read config
    .memoize
    .flatten

  override val root: Task[Config.Root] = parseConf
  override val app: Task[Config.App]   = parseConf.map(_.app)
  override val db: Task[Config.Db]     = parseConf.map(_.db)
}

object PureconfigConfigService {
  def withPureconfigConfig[R](implicit ev: R Mix Config): ZManaged[R, Throwable, R with Config] =
    ZManaged
      .environment[R]
      .map(
        ev.mix(_, new Config {
          override val config: Config.Service[Any] = new PureconfigConfigService()
        })
      )
}
