import akka.actor.ActorSystem
import akka.actor.Props
import worklogs.WorklogCommandHandler

object Global {

}

package object globals {
  val system = ActorSystem("trackit-system")
  val worklogCommandHandler = system.actorOf(Props[WorklogCommandHandler], "WorklogCommandHandler")
}