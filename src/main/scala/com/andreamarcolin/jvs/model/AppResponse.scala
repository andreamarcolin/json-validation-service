package com.andreamarcolin.jvs.model

import cats.implicits._
import io.circe.generic.auto._
import AppResponse._
import com.andreamarcolin.jvs.Main.AppTask
import org.http4s.{Response, Status}

case class AppResponse(
    action: Option[Action],
    id: Option[String],
    status: Option[ActionStatus],
    message: Option[String]
)
object AppResponse {
  sealed trait Action
  object Action {
    case object uploadSchema     extends Action
    case object downloadSchema   extends Action
    case object validateDocument extends Action
  }

  sealed trait ActionStatus
  object ActionStatus {
    case object success extends ActionStatus
    case object error   extends ActionStatus
  }

  def httpResponse(
      httpStatus: Status,
      action: Action,
      id: String,
      actionStatus: ActionStatus,
      message: Option[String] = none
  ): Response[AppTask] =
    buildHttpResponse(httpStatus, message, action.some, id.some, actionStatus.some)

  def buildHttpResponse(
      httpStatus: Status,
      message: Option[String],
      action: Option[Action] = none,
      id: Option[String] = none,
      actionStatus: Option[ActionStatus] = none
  ): Response[AppTask] =
    Response[AppTask](httpStatus).withEntity(AppResponse(action, id, actionStatus, message))
}
