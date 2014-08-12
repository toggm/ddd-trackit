package models

import play.api.libs.json.Json
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import reactivemongo.bson.BSONObjectID

sealed class WorklogCommand(issueKey:String) {
}

case class StartWorklog(worklogId:BSONObjectID, issueKey:String, userId:String, timestamp: DateTime=DateTime.now) extends WorklogCommand(issueKey)
case class EndWorklog(worklogId:BSONObjectID, issueKey:String, userId:String, timestamp:DateTime=DateTime.now, comment:Option[String]=None) extends WorklogCommand(issueKey)
case class AppendWorklog(worklogId:BSONObjectID, issueKey:String, userId:String, start:DateTime, end:DateTime, comment:Option[String]) extends WorklogCommand(issueKey)