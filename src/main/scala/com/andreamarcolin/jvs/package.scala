package com.andreamarcolin

import com.andreamarcolin.jvs.Main.AppTask
import io.circe.{Encoder, Printer}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderWithPrinterOf
import zio.ZManaged

package object jvs {
  implicit def circeAppTaskJsonEncoder[A: Encoder]: EntityEncoder[AppTask, A] =
    jsonEncoderWithPrinterOf[AppTask, A](Printer.spaces2.copy(dropNullValues = true))

  // variant with more constrained types.
  // this allows not specifying intermediate environments in main.
  implicit class ZManagedSyntax[R, E, A](zm: ZManaged[R, E, A]) {
    def >>*[E1 >: E, B](that: ZManaged[A, E1, B]): ZManaged[R, E1, B] =
      zm >>> that
  }
}
