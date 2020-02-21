package com.snowplowanalytics.jvs.model

import com.snowplowanalytics.jvs.model.AppResponse._

case class AppResponse(action: Action, id: String, status: ActionStatus, message: Option[String])
object AppResponse {
  sealed trait Action
  object Action {
    case object uploadSchema     extends Action
    case object validateDocument extends Action
  }

  sealed trait ActionStatus
  object ActionStatus {
    case object success extends ActionStatus
    case object error   extends ActionStatus
  }
}
