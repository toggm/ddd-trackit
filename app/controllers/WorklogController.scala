package controllers

import play.api.mvc.Controller
import org.joda.time.DateTime
import dao.CommonDAOComponent
import dao.MongoCommonDAOComponent
import play.api.mvc.Action
import play.api.mvc.Result
import core.Secured
import play.api.libs.concurrent.Execution.Implicits._
import models.AppendWorklog
import reactivemongo.bson.BSONObjectID
import models.StartWorklog
import globals._
import scala.concurrent.Future
import views.html.defaultpages.badRequest
import akka.actor.Props
import worklogs.UserWorklog
import models.User
import models.StopWorklog

class WorklogController extends Controller with Secured {
  this: CommonDAOComponent =>

  def worklogCommandHandler(implicit user: User) = system.actorOf(UserWorklog.props(user), "my-user-worklog")

  def appendWorklog(issueKey: String, start: DateTime, end: DateTime, comment: Option[String] = None) =
    withUser { implicit user =>
      request =>
        worklogCommandHandler ! AppendWorklog(issueKey, start, end, comment)
        Future.successful(Ok(""))
    }

  def startWorklog(issueKey: String, start: DateTime = DateTime.now) = {
    withUser { implicit user =>
      request =>
        worklogCommandHandler ! StartWorklog(issueKey, start)
        Future.successful(Ok(""))
    }
  }

  def stopWorklog(issueKey: String, end: DateTime = DateTime.now, comment: Option[String] = None) = {
    withUser { implicit user =>
      request =>
        worklogCommandHandler ! StopWorklog(issueKey, end, comment)
        Future.successful(Ok(""))
    }
  }
}

object WorklogController extends WorklogController with MongoCommonDAOComponent {
}