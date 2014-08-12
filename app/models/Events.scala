package models

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

sealed class WorklogEvent() {
}

case class WorklogCreated(worklogId: BSONObjectID, issueKey: String, userId:String, start:DateTime, end:Option[DateTime]=None, comment:Option[String]=None) extends WorklogEvent
case class WorklogChanged(worklogId: BSONObjectID, userId:String, start:Option[DateTime], end:Option[DateTime], comment:Option[String]) extends WorklogEvent
