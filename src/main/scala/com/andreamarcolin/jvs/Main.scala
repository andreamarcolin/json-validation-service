package com.andreamarcolin.jvs

import cats.effect._
import com.andreamarcolin.jvs.config.Config
import com.andreamarcolin.jvs.config.PureconfigConfigService._
import fs2.Stream.Compiler._
import com.andreamarcolin.jvs.Web._
import com.andreamarcolin.jvs.repository._
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.logging.slf4j.Slf4jLogger
import zio.logging.Logging
import zio.macros.delegate.Mix

object Main extends ManagedApp {
  type AppEnvironment = Clock with Console with Blocking with Config with Logging with SchemaRepository
  type AppTask[A]     = RIO[AppEnvironment, A]

  override def run(args: List[String]): ZManaged[ZEnv, Nothing, Int] =
    buildEnvironment
      .flatMap(serve.provide)
      .foldM(
        err => putStrLn(s"Execution failed with: $err").as(1).toManaged_,
        _ => ZManaged.succeed(0)
      )

  private def buildEnvironment: ZManaged[ZEnv, Throwable, ZEnv with Config with Logging with SchemaRepository] =
    ZManaged.environment[ZEnv] >>*
      withPureconfigConfig >>*
      withSlf4jLogger >>*
      withDoobieRepositories

  private def serve: ZManaged[AppEnvironment, Throwable, Unit] =
    for {
      implicit0(rts: Runtime[AppEnvironment]) <- ZIO.runtime[AppEnvironment].toManaged_
      appConf                                 <- config.app.toManaged_
      _ <- BlazeServerBuilder[AppTask]
            .bindHttp(appConf.port, "0.0.0.0")
            .withHttpApp(buildRouter(appConf.baseUrl))
            .serve
            .compile[AppTask, AppTask, ExitCode]
            .drain
            .toManaged_
    } yield ()

  private def withSlf4jLogger[R](implicit ev: R Mix Logging): ZManaged[R, Throwable, R with Logging] =
    for {
      env    <- ZManaged.environment[R]
      logger <- Slf4jLogger.make((_, message) => message).toManaged_
    } yield ev.mix(env, logger)
}
