package lgasior.dicegame.api.request

import akka.actor.{ReceiveTimeout, Actor, Props, ActorRef}
import lgasior.dicegame.actor.GameManager
import lgasior.dicegame.api.GameApi.CreateGameResponseData
import org.json4s.Formats
import spray.http.StatusCodes
import spray.httpx.Json4sSupport
import spray.routing.RequestContext

import scala.concurrent.duration.Duration

object CreateGameRequestActor {
  def props(ctx: RequestContext,
            gameManager: ActorRef)
           (implicit json4sFormats: Formats,
            timeout: Duration) =
    Props(new CreateGameRequestActor(ctx, gameManager))
}

class CreateGameRequestActor(
    ctx: RequestContext,
    gameManager: ActorRef)(
    implicit override val json4sFormats: Formats,
    timeout: Duration)
  extends Actor
  with Json4sSupport {

  gameManager ! GameManager.CreateGame

  context.setReceiveTimeout(timeout)

  override def receive = {
    case GameManager.GameCreated(id) =>
      ctx.complete(StatusCodes.Created, CreateGameResponseData(id.value))
      context stop self
    case ReceiveTimeout =>
      ctx.complete(StatusCodes.RequestTimeout)
      context stop self
  }

}
