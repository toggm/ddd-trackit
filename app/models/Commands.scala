package models

import play.api.libs.json.Json
import org.joda.time.DateTime
import play.api.libs.json.JsValue

sealed class WorklogCommand(issueKey:String) {
}

case class StartWorklog(issueKey:String, timestamp: DateTime=DateTime.now) extends WorklogCommand(issueKey)
case class EndWorklog(issueKey:String, timestamp:DateTime=DateTime.now, comment:Option[String]=None) extends WorklogCommand(issueKey)
case class AppendWorklog(issueKey:String, start:DateTime, end:DateTime, comment:Option[String]) extends WorklogCommand(issueKey)

object WorklogCommand {
  def unapply(foo: WorklogCommand): Option[(String, JsValue)] = {
    val (prod: Product, sub) = foo match {
      case b: StartWorklog => (b, Json.toJson(b)(StartWorklog.startWorklogFormat))
      case b: EndWorklog => (b, Json.toJson(b)(EndWorklog.endWorklogFormat))
      case b: AppendWorklog => (b, Json.toJson(b)(AppendWorklog.appendWorklogFormat))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): WorklogCommand = {
    (`class` match {
      case "StartWorklog" => Json.fromJson[StartWorklog](data)(StartWorklog.startWorklogFormat)
      case "EndWorklog" => Json.fromJson[EndWorklog](data)(EndWorklog.endWorklogFormat)
      case "AppendWorklog" => Json.fromJson[AppendWorklog](data)(AppendWorklog.appendWorklogFormat)
    }).get
  }
  
  implicit val worklogCommandFormat = Json.format[WorklogCommand] 
}
object StartWorklog {
  implicit val startWorklogFormat = Json.format[StartWorklog]
}
object EndWorklog {
  implicit val endWorklogFormat = Json.format[EndWorklog]
}
object AppendWorklog {
  implicit val appendWorklogFormat = Json.format[AppendWorklog]
}