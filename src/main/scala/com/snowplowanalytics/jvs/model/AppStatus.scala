package com.snowplowanalytics.jvs.model

case class AppStatus(status: Status, db: Status)
object AppStatus {
  def apply(db: Status): AppStatus = AppStatus(db, db)
}
