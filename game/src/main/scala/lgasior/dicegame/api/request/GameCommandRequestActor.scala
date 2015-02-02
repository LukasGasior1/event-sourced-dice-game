package lgasior.dicegame.api.request

import akka.actor.{ReceiveTimeout, Actor, Props, ActorRef}
import lgasior.dicegame.actor.GameActor.{CommandRejected, CommandAccepted}
import lgasior.dicegame.actor.GameManager
import lgasior.dicegame.actor.GameManager.GameDoesNotExist
import lgasior.dicegame.api.GameApi.ViolationResponse
import lgasior.dicegame.domain._
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object GameCommandRequestActor {
  def props(ctx: RequestContext,
            gameManager: ActorRef,
            gameId: GameId,
            command: GameCommand)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new GameCommandRequestActor(ctx, gameManager, gameId, command))
}

class GameCommandRequestActor(
    ctx: RequestContext,
    gameManager: ActorRef,
    gameId: GameId,
    command: GameCommand)(
    implicit override val json4sFormats: Formats,
    timeout: Duration)
  extends Actor with Json4sSupport {

  gameManager ! GameManager.SendCommand(gameId, command)

  context.setReceiveTimeout(timeout)

  override def receive = {
    case CommandAccepted => ctx.complete(StatusCodes.Accepted)
    case CommandRejected(violation) =>
      val message = violation match {
        case NotEnoughPlayersViolation => "Not enough players"
        case NotCurrentPlayerViolation => "Not this player's turn"
        case GameAlreadyStartedViolation => "Game is already started"
        case GameNotRunningViolation => "Game is not started"
      }
      ctx.complete(StatusCodes.BadRequest, ViolationResponse(message))
      context stop self
    case GameDoesNotExist =>
      ctx.complete(StatusCodes.NotFound)
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
