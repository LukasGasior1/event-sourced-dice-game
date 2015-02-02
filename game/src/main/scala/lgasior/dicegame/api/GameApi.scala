package lgasior.dicegame.api

import akka.actor.ActorRef
import lgasior.dicegame.api.request.{CreateGameRequestActor, GameCommandRequestActor}
import lgasior.dicegame.domain._
import org.json4s.DefaultFormats

import spray.httpx.Json4sSupport
import spray.routing.{Route, HttpService}

import scala.concurrent.duration._

object GameApi {
  case class CreateGameResponseData(id: String)
  case class StartGameRequestData(players: Seq[String])
  case class ViolationResponse(message: String)
}

trait GameApi extends HttpService with Json4sSupport {

  import GameApi._

  override implicit val json4sFormats = DefaultFormats

  implicit val timeout = 2.seconds

  def gameManager: ActorRef

  val gameRoute =
    (pathPrefix("game") & post) {
      pathEndOrSingleSlash(handleCreate) ~
      (path(Segment / "start") & entity(as[StartGameRequestData]))(handleStart) ~
      path(Segment / "roll" / Segment)(handleRoll)
    }

  private def handleCreate: Route = { ctx =>
    actorRefFactory.actorOf(CreateGameRequestActor.props(ctx, gameManager))
  }

  private def handleStart(gameId: String, requestData: StartGameRequestData) =
    handleGameCommand(gameId, StartGame(requestData.players.map(PlayerId.apply)))

  private def handleRoll(gameId: String, playerId: String) =
    handleGameCommand(gameId, RollDice(PlayerId(playerId)))

  private def handleGameCommand(gameId: String, command: GameCommand): Route = { ctx =>
    val props = GameCommandRequestActor.props(ctx, gameManager, GameId(gameId), command)
    actorRefFactory.actorOf(props)
  }

}
