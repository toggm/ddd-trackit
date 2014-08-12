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
import models.EndWorklog
import views.html.defaultpages.badRequest

class WorklogController extends Controller with Secured {
  this: CommonDAOComponent =>
  def appendWorklog(issueKey: String, start: DateTime, end: DateTime, comment: Option[String] = None) =
    withUser { implicit user =>
      request =>
        val cmd = AppendWorklog(BSONObjectID.generate, issueKey, user.userId, start, end, comment)
        worklogCommandHandler ! cmd
        Future.successful(Ok(cmd.worklogId.stringify))
    }

  def startWorklog(issueKey: String, start: DateTime = DateTime.now) = {
    withUser { implicit user =>
      request =>
        val cmd = StartWorklog(BSONObjectID.generate, issueKey, user.userId, start)
        worklogCommandHandler ! cmd
        Future.successful(Ok(cmd.worklogId.stringify))
    }
  }

  def endWorklog(worklogId: String, issueKey: String, end: DateTime = DateTime.now, comment: Option[String] = None) = {
    withUser { implicit user =>
      request =>
        val parsedId = BSONObjectID.parse(worklogId)
        if (parsedId.isSuccess) {
          val cmd = EndWorklog(parsedId.get, issueKey, user.userId, end, comment)
          worklogCommandHandler ! cmd
          Future.successful(Ok(cmd.worklogId.stringify))
        } else {
          Future.successful(error(s"Couldn't parse id: $worklogId"))
        }
    }
  }
}

object WorklogController extends WorklogController with MongoCommonDAOComponent {
}