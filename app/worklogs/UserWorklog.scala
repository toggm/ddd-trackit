package worklogs

import akka.actor.Props
import akka.actor.Status.Failure
import akka.actor.actorRef2Scala
import akka.persistence.EventsourcedProcessor
import models.AppendWorklog
import models.StartWorklog
import models.User
import models.Worklog
import models.WorklogEvent
import reactivemongo.bson.BSONObjectID
import dao.CommonDAOComponent
import models.WorkStarted
import models.StopWorklog
import models.WorkStopped
import models.WorkCommented
import ddd.AggregateState
import models.WorkStarted
import models.WorkStopped
import models.WorkLogged
import models.WorkCommented
import models.WorkStopped
import ddd.AggregateRoot

case class WorklogCreationException(message: String)
  extends RuntimeException(message)

case class WorklogChangeException(message: String)
  extends RuntimeException(message)

case class Worklogs(worklogs: Seq[Worklog]) extends AggregateState {

  def exists(worklogId: BSONObjectID): Boolean = {
    //check if worklog with same id already exists	
    worklogs.exists(_.id == worklogId)
  }

  def findOpenWorklog(issueId: String): Option[Worklog] = {
    worklogs.find(_.issueKey == issueId).headOption
  }

  override def apply = {
    case event: WorkStarted =>
      val newWorklogs = worklogs :+ Worklog(event.worklogId, event.issueId, event.userId, event.start, None, None, false)
      copy(worklogs = newWorklogs)
    case event: WorkStopped =>
      val newWorklogs = worklogs.find(w => w.id == event.worklogId) match {
        case Some(worklog) =>
          val index = worklogs.indexOf(worklog)
          worklogs.updated(index, worklog.copy(end = Some(event.end)))
      }
      copy(worklogs = newWorklogs)
    case event: WorkLogged =>
      val newWorklogs = worklogs :+ Worklog(event.worklogId, event.issueId, event.userId, event.start, Some(event.end), None, false)
      copy(worklogs = newWorklogs)
    case event: WorkCommented =>
      val newWorklogs = worklogs.find(w => w.id == event.worklogId) match {
        case Some(worklog) =>
          val index = worklogs.indexOf(worklog)
          worklogs.updated(index, worklog.copy(comment = Some(event.comment)))
      }
      copy(worklogs = newWorklogs)
  }
}

class UserWorklog(user: User) extends AggregateRoot[Worklogs, WorklogEvent] {
  this: CommonDAOComponent =>
  override type EventHandler = WorklogEvent => Unit
  var worklogs: Worklogs = Worklogs(Seq())

  val factory: AggregateRootFactory = PartialFunction[WorklogEvent, Worklogs] {
    case e: StartWorklog => Worklogs(Seq())
  }

  val receiveCommand: Receive = {
    case StartWorklog(issueId, start) =>

      //check if there is another open worklog, if so, check if creat event is a 'work in progress' event
      //if so, stop other worklog with start time of new worklog as end time
      worklogs.worklogs.filter(w => w.userId == user.userId && w.end != null).map(w => raise(WorkStopped(w.id, w.issueKey, w.userId, start)))

      raise(WorkStarted(BSONObjectID.generate, issueId, user.userId, start))
    case StopWorklog(issueId, end, comment) =>
      worklogs.findOpenWorklog(issueId).map { w =>
        comment.map(c => raise(WorkCommented(w.id, issueId, user.userId, c)))
        raise(WorkStopped(w.id, issueId, user.userId, end))
      }.getOrElse {
        throw new WorklogChangeException(s"No open worklog found for issue $issueId")
      }
    case AppendWorklog(issueId, start, end, comment) =>
      val worklogId = BSONObjectID.generate
      raise(WorkStarted(worklogId, issueId, user.userId, start))
      comment.map(c => raise(WorkCommented(worklogId, issueId, user.userId, c)))
      raise(WorkStopped(worklogId, issueId, user.userId, end))
  }
}

object UserWorklog {
  def props(user: User) = Props(classOf[Worklog], user)
}