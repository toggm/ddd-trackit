package worklogs

import akka.actor.Actor
import dao.CommonDAOComponent
import models.Worklog
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.WorklogEvent
import models.WorkStarted
import models.WorkStopped
import models.WorkLogged
import models.WorkCommented

class WorklogDAOEventHandler extends Actor {
  this: CommonDAOComponent =>

  override def preStart = {
    //register by default
    context.system.eventStream.subscribe(context.self, classOf[WorklogEvent])
  }

  def receive = {
    case WorkStarted(worklogId, issueKey, userId, start) =>
      worklogDAO.save(Worklog(worklogId, issueKey, userId, start, None, None, false))
    case WorkStopped(worklogId, issueKey, userId, end) =>
      worklogDAO.get(worklogId).map { o =>
        o.map(w => worklogDAO.save(w.copy(end = Some(end))))
      }
    case WorkLogged(worklogId, issueKey, userId, start, end) =>
      worklogDAO.save(Worklog(worklogId, issueKey, userId, start, Some(end), None, false))
    case WorkCommented(worklogId, issueKey, userId, comment) =>
      worklogDAO.get(worklogId).map { o =>
        o.map(w => worklogDAO.save(w.copy(comment = Some(comment))))
      }
  }
}
