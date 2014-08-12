package worklogs

import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import akka.persistence.EventsourcedProcessor
import models.AppendWorklog
import models.EndWorklog
import models.StartWorklog
import models.User
import models.Worklog
import models.WorklogChanged
import models.WorklogCreated
import models.WorklogEvent
import reactivemongo.bson.BSONObjectID

case class WorklogCreationException(message: String)
  extends RuntimeException(message)

case class WorklogChangeException(message: String)
  extends RuntimeException(message)

case class Worklogs(worklogs: Seq[Worklog]) {

  def exists(worklogId: BSONObjectID): Boolean = {
    //check if worklog with same id already exists	
    worklogs.exists(_.id == worklogId)
  }

  def created(event: WorklogCreated): Worklogs = {

    //check if there is another open worklog, if so, check if creat event is a 'work in progress' event
    if (event.end.isEmpty) {
      //if so, stop other worklog with start time of new worklog as end time
      worklogs.filter(w => w.userId == event.userId && w.end != null).map(w => changed(WorklogChanged(w.id, w.userId, None, Some(event.start), None)))
    }

    Worklogs(worklogs :+ Worklog(event.worklogId, event.issueKey, event.userId, event.start, event.end, event.comment, false))
  }

  def changed(event: WorklogChanged): Worklogs = {

    Worklogs(worklogs.map { w =>
      if (w.id == event.worklogId) {
        w.update(event)
      } else {
        w
      }
    }.filter(!_.commited)) //keep only non published worklogs
  }
}

case object Acknowledged

class WorklogCommandHandler extends EventsourcedProcessor {

  type EventHandler = WorklogEvent => Unit

  var worklogs: Worklogs = Worklogs(Seq())

  def updateState(event: WorklogEvent) = {
    event match {
      case e: WorklogCreated => worklogs = worklogs.created(e)
      case e: WorklogChanged => worklogs = worklogs.changed(e)
    }
  }

  val receiveRecover: Receive = {
    case evt: WorklogEvent => updateState(evt)
  }

  val receiveCommand: Receive = {
    case StartWorklog(worklogId, issueId, userId, start) =>
      //check if worklog with same id already exists	
      if (worklogs.exists(worklogId)) {
        throw new WorklogCreationException("Worklog with same id already exists")
      }

      raise(WorklogCreated(worklogId, issueId, userId, start))
    case EndWorklog(worklogId, issueId, userId, end, comment) =>
      //check if worklog with same id already exists	
      if (!worklogs.exists(worklogId)) {
        throw new WorklogChangeException(s"Worklog with $worklogId not found")
      }

      raise(WorklogChanged(worklogId, userId, None, Some(end), comment))
    case AppendWorklog(worklogId, issueId, userId, start, end, comment) =>
      //check if worklog with same id already exists	
      if (worklogs.exists(worklogId)) {
        throw new WorklogCreationException("Worklog with same id already exists")
      }

      raise(WorklogCreated(worklogId, issueId, userId, start, Some(end), comment))
  }

  def raise(event: WorklogEvent)(implicit handler: EventHandler = handle) {
    persist(event) {
      persistedEvent =>
        {
          updateState(persistedEvent)
          handler(persistedEvent)
        }
    }
  }
  def handle(event: WorklogEvent) {
    publish(event)
    sender() ! Acknowledged
  }

  def publish(event: WorklogEvent) {
    context.system.eventStream.publish(event)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }
}

object WorklogCommandHandler {
  def props(user: User) = Props(classOf[WorklogCommandHandler], user)
}