package com.andreamarcolin.jvs

import sourcecode.{File, Line, Pkg}
import zio.{URIO, ZIO}

package object logger extends Logger.Service[Logger] {

  override def trace(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.trace(msg, args))

  override def debug(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.debug(msg, args))

  override def info(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.info(msg, args))

  override def warn(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.warn(msg, args))

  override def error(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.error(msg, args))

  override def error(msg: String, cause: Throwable)(implicit pkg: Pkg, file: File, line: Line): URIO[Logger, Unit] =
    ZIO.accessM(_.log.error(msg, cause))
}
