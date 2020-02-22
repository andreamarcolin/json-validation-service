package com.andreamarcolin.jvs.logger

import sourcecode.{File, Line, Pkg}
import zio.URIO

trait Logger extends Serializable { val log: Logger.Service[Any] }
object Logger {

  trait Service[R] extends Serializable {
    def trace(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
    def debug(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
    def info(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
    def warn(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
    def error(msg: String, args: AnyRef*)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
    def error(msg: String, cause: Throwable)(implicit pkg: Pkg, file: File, line: Line): URIO[R, Unit]
  }

}
