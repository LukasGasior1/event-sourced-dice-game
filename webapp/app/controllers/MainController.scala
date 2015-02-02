package controllers

import actors.WebsocketEventPublisher
import models.GameEvent
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.libs.ws._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object MainController extends Controller {

  import config.Config.Game._

  implicit val gameEventFormat = Json.format[GameEvent]

  implicit val gameEventFrameFormatter = FrameFormatter.jsonFrame[GameEvent]

  def index() = Action { request =>
    Ok(views.html.index())
  }

  def createGame() = Action.async { request =>
    WS.url(s"$apiUrl/game")
      .post("")
      .map {
        case res if res.status == CREATED => Created(res.body)
        case _ => InternalServerError
      }
      .recover { case _ => InternalServerError }
  }

  def startGame(gameId: String, playersCount: Int) = Action.async { request =>
    val players = 1 to playersCount map { n => s"Player$n"}
    postCommand(
      url = s"/game/$gameId/start",
      data = Json.obj("players" -> players))
  }

  def roll(gameId: String, playerId: String) = Action.async { request =>
    postCommand(
      url = s"/game/$gameId/roll/$playerId",
      data = Json.obj())
  }

  private def postCommand(url: String, data: JsValue) = {
    WS.url(s"$apiUrl$url")
      .post(data)
      .map {
        case res if res.status == ACCEPTED => Accepted
        case res if res.status == BAD_REQUEST => BadRequest(res.body)
        case res => println(res.status); println(res.body); InternalServerError
      }
      .recover { case _ => InternalServerError }
  }

  def gameEvents(gameId: String) = WebSocket.acceptWithActor[String, GameEvent] { request => out =>
    WebsocketEventPublisher.props(gameId, out)
  }

}
