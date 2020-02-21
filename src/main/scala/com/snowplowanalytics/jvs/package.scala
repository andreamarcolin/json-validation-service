package com.snowplowanalytics

import com.snowplowanalytics.jvs.Main.AppTask
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import zio.ZManaged
import zio.interop.catz._

package object jvs {

  implicit def circeAppTaskJsonDecoder[A: Decoder]: EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeAppTaskJsonEncoder[A: Encoder]: EntityEncoder[AppTask, A] = jsonEncoderOf[AppTask, A]

  // variant with more constrained types.
  // this allows not specifying intermediate environments in main.
  implicit class ZManagedSyntax[R, E, A](zm: ZManaged[R, E, A]) {
    def >>*[E1 >: E, B](that: ZManaged[A, E1, B]): ZManaged[R, E1, B] =
      zm >>> that
  }

}
