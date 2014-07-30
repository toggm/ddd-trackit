package models

import play.api.libs.json.Json
import org.joda.time.DateTime

sealed trait IssueCommand {
}

case class StartTracking(issueKey:String, timestamp: DateTime) extends IssueCommand
case class EndTracking(issueKey:String, timestamp:DateTime) extends IssueCommand
case class AddComment(issueKey:String, comment:String) extends IssueCommand
