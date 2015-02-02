package lgasior.dicegame.statistics

import akka.actor.ActorSystem
import akka.io.IO
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.scalac.amqp.{Connection, Queue}
import lgasior.dicegame.statistics.actor.{SubscriberActor, StatsActor}
import lgasior.dicegame.statistics.api.StatsApiServiceActor
import lgasior.dicegame.statistics.config.Config
import org.slf4j.LoggerFactory
import spray.can.Http

import scala.util.{Success, Failure}

object StatsApp {

  implicit val system = ActorSystem("StatisticsSystem")
  import system.dispatcher

  val log = LoggerFactory.getLogger(StatsApp.getClass)

  val statsActor = system.actorOf(StatsActor.props, "stats")

  def main(args: Array[String]) = {
    setupRestApi()
    setupEventStreamConsumption()
  }

  private def setupRestApi() = {
    import Config.Api._
    val statsApiServiceActor = system.actorOf(StatsApiServiceActor.props(statsActor), "stats-api-service")
    IO(Http).tell(Http.Bind(statsApiServiceActor, bindHost, bindPort), statsApiServiceActor)
  }

  private def setupEventStreamConsumption() = {
    import Config.Events._

    implicit val connection = Connection()
    val queue = Queue(queueName, durable = false, autoDelete = true)
    val bindArgs = Map("x-match" -> "all", "type" -> "DiceRolled")

    val resultFuture = for {
      _ <- connection.queueDeclare(queue)
      _ <- connection.queueBind(queueName, exchangeName, "", bindArgs)
    } yield ()

    resultFuture onComplete {
      case Success(_) =>
        Source(connection.consume(queueName))
          .map(_.message)
          .to(Sink(SubscriberActor.props(statsActor)))
          .run()(ActorFlowMaterializer())
      case Failure(ex) =>
        log.error("Cannot setup queue", ex)
        sys.exit(1)
    }

  }

}
