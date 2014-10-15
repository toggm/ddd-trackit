package models

import play.api.libs.json.Json
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import reactivemongo.bson.BSONObjectID

sealed class WorklogCommand(issueKey: String) {
}

case class StartWorklog(issueKey: String, timestamp: DateTime = DateTime.now) extends WorklogCommand(issueKey)
case class StopWorklog(issueKey: String, timestamp: DateTime = DateTime.now, comment: Option[String] = None) extends WorklogCommand(issueKey)
case class AppendWorklog(issueKey: String, start: DateTime, end: DateTime, comment: Option[String]) extends WorklogCommand(issueKey)