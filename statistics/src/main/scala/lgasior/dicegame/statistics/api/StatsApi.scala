package lgasior.dicegame.statistics.api

import akka.actor.ActorRef
import lgasior.dicegame.statistics.api.request.GetStatsRequestActor
import org.json4s.DefaultFormats
import spray.routing.{Route, HttpService}

object StatsApi {
  case class RollCount(rolledNumber: Int, rollsCount: Int)
  case class StatsResponseData(rollsCounts: Seq[RollCount])
}

trait StatsApi extends HttpService {

  implicit val formats = DefaultFormats

  def statsActor: ActorRef

  val statsRoute = (path("stats") & get)(handleGetStats)

  private def handleGetStats: Route = { ctx =>
    actorRefFactory.actorOf(GetStatsRequestActor.props(ctx, statsActor))
  }

}
