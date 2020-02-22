package com.andreamarcolin.jvs.logger

import org.slf4j.{LoggerFactory, Logger => SLogger}
import sourcecode.{File, Line, Pkg}
import zio.macros.delegate.Mix
import zio.{UIO, ZIO, ZManaged}

final class Slf4jLogger private (private val logger: SLogger) extends Logger.Service[Any] {

  override def trace(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isTraceEnabled) logger.trace(messageWithContext(msg), args))

  override def debug(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isDebugEnabled) logger.debug(messageWithContext(msg), args))

  override def info(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isInfoEnabled) logger.info(messageWithContext(msg), args))

  override def warn(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isWarnEnabled) logger.warn(messageWithContext(msg), args))

  override def error(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isErrorEnabled) logger.error(messageWithContext(msg), args))

  override def error(msg: String, cause: Throwable)(implicit pkg: Pkg, file: File, line: Line): UIO[Unit] =
    ZIO.effectTotal(if (logger.isErrorEnabled) logger.error(messageWithContext(msg), cause))

  private def messageWithContext(msg: String)(implicit pkg: Pkg, file: File, line: Line): String =
    pkg.value.replaceFirst("com\\.snowplowanalytics\\.jvs\\.?", "") +
      s"${file.value.split("/").last.stripSuffix(".scala")}:${line.value} - $msg"
}

object Slf4jLogger {

  def withSlf4jLogger[R](implicit ev: R Mix Logger): ZManaged[R, Nothing, R with Logger] =
    ZManaged
      .environment[R]
      .map(
        ev.mix(_, new Logger {
          val log = new Slf4jLogger(LoggerFactory.getLogger("com.snowplowanalytics.jvs"))
        })
      )

}
