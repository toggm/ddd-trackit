package ddd

import akka.actor.ActorLogging
import akka.persistence.EventsourcedProcessor
import akka.actor.Status.Failure

trait DomainEvent extends Serializable

case object Acknowledged

trait DomainException extends RuntimeException

class AggregateRootNotInitializedException extends DomainException {
}

trait AggregateState {
  type StateMachine = PartialFunction[DomainEvent, AggregateState]
  def apply: StateMachine
}

trait AggregateRoot[S <: AggregateState, E <: DomainEvent] extends EventsourcedProcessor with ActorLogging {
  type AggregateRootFactory = PartialFunction[E, S]
  type EventHandler = E => Unit
  private var stateOpt: Option[S] = None
  val factory: AggregateRootFactory
  override def receiveRecover: Receive = {
    case evt: E => updateState(evt)
  }

  private def updateState(event: E) {
    val nextState = if (initialized) state.apply(event) else factory.apply(event)
    stateOpt = Option(nextState.asInstanceOf[S])
  }

  def raise(event: E)(implicit handler: EventHandler = handle) {
    persist(event) {
      persistedEvent =>
        {
          log.info("Event persisted: {}", event)
          updateState(persistedEvent)
          handler(persistedEvent)
        }
    }
  }

  def handle(event: E) {
    publish(event)
    sender() ! Acknowledged
  }

  def publish(event: E) {
    context.system.eventStream.publish(event)
  }

  def initialized = stateOpt.isDefined
  protected def state = if (initialized) stateOpt.get else throw new AggregateRootNotInitializedException
  override def preRestart(reason: Throwable, message: Option[Any]) {
    sender() ! Failure(reason)
    super.preRestart(reason, message)
  }
}
