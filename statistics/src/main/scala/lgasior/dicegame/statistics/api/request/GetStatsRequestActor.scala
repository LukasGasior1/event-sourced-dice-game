package lgasior.dicegame.statistics.api.request

import akka.actor.{ReceiveTimeout, Actor, Props, ActorRef}
import lgasior.dicegame.statistics.Stats
import lgasior.dicegame.statistics.actor.StatsActor
import lgasior.dicegame.statistics.api.StatsApi.{StatsResponseData, RollCount}
import org.json4s.Formats
import scala.concurrent.duration._
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

object GetStatsRequestActor {
  def props(ctx: RequestContext,
            statsActor: ActorRef)(implicit json4sFormats: Formats) =
    Props(new GetStatsRequestActor(ctx, statsActor))
}

class GetStatsRequestActor(
    ctx: RequestContext,
    statsActor: ActorRef)(implicit override val json4sFormats: Formats)
  extends Actor
  with Json4sSupport {

  statsActor ! StatsActor.GetState

  context.setReceiveTimeout(2.seconds)

  override def receive = {
    case stats: Stats =>
      val rollsCounts = stats.rollsCounts
        .map(RollCount.tupled)
        .toSeq
        .sortBy(_.rollsCount)
        .reverse
      ctx.complete(StatsResponseData(rollsCounts))
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
