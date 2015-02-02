package actors

import akka.actor._
import akka.stream.actor.ActorSubscriberMessage.{OnError, OnComplete, OnNext}
import akka.stream.actor.{RequestStrategy, ActorSubscriber}
import io.scalac.amqp.Message
import models.GameEvent
import play.api.libs.json.Json

object EventSubscriber {
  def props(publisher: ActorRef) = Props(new EventSubscriber(publisher))
}

class EventSubscriber(publisher: ActorRef) extends ActorSubscriber with ActorLogging {

  override val requestStrategy = new RequestStrategy {
    override def requestDemand(remainingRequested: Int) =
      Math.max(remainingRequested, 1)
  }

  override def receive = {
    case OnNext(message: Message) =>
      val gameEvent =
        GameEvent(
          gameId = message.headers("gameId"),
          eventType = message.headers("type"),
          data = Json.parse(message.body.decodeString("UTF-8")))
      publisher ! gameEvent
    case OnComplete =>
      log.info("Game event stream completed")
      context unwatch publisher
      publisher ! PoisonPill
      context stop self
    case OnError(cause) =>
      log.error(cause, "Subscriber error occurred")
      context unwatch publisher
      publisher ! PoisonPill
      context stop self
  }

}
