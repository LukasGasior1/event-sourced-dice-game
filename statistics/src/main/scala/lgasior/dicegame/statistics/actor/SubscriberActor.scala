package lgasior.dicegame.statistics.actor

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, RequestStrategy}
import io.scalac.amqp.Message
import org.json4s.JsonAST.JInt
import org.json4s.native.JsonMethods._

object SubscriberActor {
  def props(statsActor: ActorRef) = Props(new SubscriberActor(statsActor))
}

class SubscriberActor(statsActor: ActorRef) extends ActorSubscriber with ActorLogging {

  import context.system

  override protected def requestStrategy = new RequestStrategy {
    override def requestDemand(remainingRequested: Int) =
      Math.max(remainingRequested, 1)
  }

  override def receive = {
    case OnNext(message: Message) =>
      for {
        eventType <- message.headers.get("type") if eventType == "DiceRolled"
        content <- parseOpt(message.body.decodeString("UTF-8"))
        JInt(rolledNumber) <- content \ "rolledNumber"
      } statsActor ! StatsActor.IncRollsCount(rolledNumber.toInt)
    case OnComplete =>
      log.info("Game stream completed, shutting down system")
      system.shutdown()
    case OnError(cause) =>
      log.error(cause, "Publisher error occurred, shutting down system")
      system.shutdown()
  }

}
