package worklogs

import akka.actor.Actor
import dao.CommonDAOComponent
import models.Worklog
import models.WorklogChanged
import models.WorklogCreated
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class WorklogDAOEventHandler extends Actor {
  this: CommonDAOComponent =>

  override def preStart = {
    //register by default
    context.system.eventStream.subscribe(context.self, classOf[WorklogCreated])
    context.system.eventStream.subscribe(context.self, classOf[WorklogChanged])
  }

  def receive = {
    case WorklogCreated(worklogId, issueKey, userId, start, end, comment) =>
      worklogDAO.insert(Worklog(worklogId, issueKey, userId, start, end, comment, false))
    case evt: WorklogChanged => {
      worklogDAO.get(evt.worklogId) map { option =>
        option map {
          case (w, _) =>
            //update record
            worklogDAO.insert(w.update(evt))
        }
      }
    }
  }
}
