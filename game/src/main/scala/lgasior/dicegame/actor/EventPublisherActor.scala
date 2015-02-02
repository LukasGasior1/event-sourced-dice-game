package lgasior.dicegame.actor

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import lgasior.dicegame.domain.GameEvent

object EventPublisherActor {
  def props = Props[EventPublisherActor]
}

class EventPublisherActor extends ActorPublisher[GameEvent] {

  var eventCache: List[GameEvent] = Nil

  context.system.eventStream.subscribe(self, classOf[GameEvent])

  override def receive = {
    case Request(n) =>
      while (isActive && totalDemand > 0 && eventCache.nonEmpty) {
        val (head :: tail) = eventCache
        onNext(head)
        eventCache = tail
      }
    case event: GameEvent =>
      if (isActive && totalDemand > 0)
        onNext(event)
      else
        eventCache :+= event
  }

}
