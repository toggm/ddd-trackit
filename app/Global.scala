import akka.actor.ActorSystem
import akka.actor.Props

object Global {

}

package object globals {
  val system = ActorSystem("trackit-system")
}