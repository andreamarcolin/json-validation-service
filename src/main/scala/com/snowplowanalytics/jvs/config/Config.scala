package com.snowplowanalytics.jvs.config

import zio.RIO

trait Config { val config: Config.Service[Any] }
object Config {

  final case class Root(app: App, db: Db)
  final case class App(port: Int, baseUrl: String)
  final case class Db(url: String, driver: String, user: String, password: String)

  trait Service[R] {
    def root: RIO[R, Config.Root]
    def app: RIO[R, Config.App]
    def db: RIO[R, Config.Db]
  }

}
