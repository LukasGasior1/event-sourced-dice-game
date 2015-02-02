package lgasior.dicegame

import akka.actor.ActorSystem
import akka.io.IO
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.google.common.net.MediaType
import io.scalac.amqp.{Headers, Exchange, Message, Connection}
import lgasior.dicegame.actor.{EventPublisherActor, GameManager}
import lgasior.dicegame.api.GameApiServiceActor
import lgasior.dicegame.config.Config
import lgasior.dicegame.domain.{PlayerId, GameEvent}
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory
import spray.can.Http

import scala.util.{Failure, Success}

object Boot {

  implicit val system = ActorSystem("DiceGameSystem")
  import system.dispatcher

  val log = LoggerFactory.getLogger(Boot.getClass)

  object PlayerIdSerializer extends CustomSerializer[PlayerId](implicit formats =>
    (PartialFunction.empty, { case playerId: PlayerId => JString(playerId.value) }))

  implicit val formats = DefaultFormats + PlayerIdSerializer

  def main(args: Array[String]) = {
    setupRestApi()
    setupEventStream()
  }

  private def setupRestApi() = {
    import Config.Api._
    val gameManager = system.actorOf(GameManager.props, "game-manager")
    val gameApiServiceActor = system.actorOf(GameApiServiceActor.props(gameManager), "game-api-service")
    IO(Http).tell(Http.Bind(gameApiServiceActor, bindHost, bindPort), gameApiServiceActor)
  }

  private def setupEventStream() = {
    import Config.Events._

    val connection = Connection()
    val exchange = Exchange(exchangeName, Headers, durable = false)

    connection.exchangeDeclare(exchange) onComplete {
      case Success(_) =>
        Source[GameEvent](EventPublisherActor.props)
          .map(toMessage)
          .to(Sink(connection.publish(exchange = exchangeName, "")))
          .run()(ActorFlowMaterializer())
      case Failure(ex) =>
        log.error("Cannot create exchange", ex)
        sys.exit(1)
    }
  }

  private def toMessage(event: GameEvent) = {
    val serialized = compact(render(Extraction.decompose(event)))
    Message(
      body = ByteString(serialized),
      contentType = Some(MediaType.JSON_UTF_8),
      contentEncoding = Some("UTF-8"),
      headers = Map(
        "gameId" -> event.id.value,
        "type" -> event.getClass.getSimpleName))
  }

}
