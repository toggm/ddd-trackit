package models

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import ddd.DomainEvent

sealed class WorklogEvent() extends DomainEvent {
}

case class WorkStarted(worklogId: BSONObjectID, issueId: String, userId: String, start: DateTime) extends WorklogEvent
case class WorkLogged(worklogId: BSONObjectID, issueId: String, userId: String, start: DateTime, end: DateTime) extends WorklogEvent
case class WorkStopped(worklogId: BSONObjectID, issueId: String, userId: String, end: DateTime) extends WorklogEvent
case class WorkCommented(worklogId: BSONObjectID, issueId: String, userId: String, comment: String) extends WorklogEvent