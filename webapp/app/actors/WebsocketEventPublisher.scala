package actors

import akka.actor._
import akka.stream.scaladsl.{ImplicitFlowMaterializer, Sink, Source}
import config.Config
import global.Global
import io.scalac.amqp.Queue
import models.GameEvent

object WebsocketEventPublisher {
  def props(gameId: String, out: ActorRef) =
    Props(new WebsocketEventPublisher(gameId, out))
}

class WebsocketEventPublisher(gameId: String, out: ActorRef)
  extends Actor
  with ActorLogging
  with ImplicitFlowMaterializer {

  import context.dispatcher

  override def preStart() = {
    import Global.connection
    import Config.Events._

    val queue = Queue(name = gameId, durable = false, autoDelete = true)

    val bindFuture = for {
      _ <- connection.queueDeclare(queue)
      _ <- connection.queueBind(queue.name, exchangeName, "", Map("gameId" -> gameId))
    } yield ()

    bindFuture.map { _ =>
      Source(connection.consume(queue.name))
        .map(_.message)
        .to(Sink(EventSubscriber.props(self)))
        .run()
    }.failed.map { ex =>
      log.error(ex, "Cannot bind queue to events from game {}", gameId)
      context stop self
    }
  }

  override def receive = {
    case ev: GameEvent if ev.gameId == gameId =>
      out ! ev
  }

}
