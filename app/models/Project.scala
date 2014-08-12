package models

import reactivemongo.bson.BSONObjectID
import play.api.libs.json.Json
import models.BaseFormats._
import org.joda.time.DateTime

case class Project(key:String, systemId: BSONObjectID, name:String, description:String="")
case class Issue(key:String, projectKey:String)
case class User(userId: String, username:String, password:String)
case class Worklog(id: BSONObjectID, issueKey: String, userId: String, start:DateTime, end:Option[DateTime], comment:Option[String], commited:Boolean) {
  def update(evt: WorklogChanged): Worklog =  {
    Worklog(id, issueKey, userId, evt.start.map(identity).getOrElse(start), evt.end.map(Some(_)).getOrElse(end), evt.comment.map(Some(_)).getOrElse(comment), published)   
  }
}

object Project {
  implicit val projectFormat = Json.format[Project]
}
object Issue {
  implicit val issueFormat = Json.format[Issue]
}
object User {
  implicit val userFormat = Json.format[User]
}
object Worklog {
  implicit val worklogFormat = Json.format[Worklog]
}
