package lgasior.dicegame.actor

import akka.actor.{Props, Actor}

import lgasior.dicegame.domain.{GameCommand, GameId}

object GameManager {
  def props = Props[GameManager]

  sealed trait Command
  case object CreateGame extends Command
  case class SendCommand(gameId: GameId, command: GameCommand) extends Command

  case class GameCreated(id: GameId)

  case object GameDoesNotExist
}

class GameManager extends Actor {

  import GameManager._

  override def receive = {
    case CreateGame =>
      val id = GameId.createRandom
      context.actorOf(GameActor.props(id), id.value)
      sender() ! GameCreated(id)
    case SendCommand(gameId, command) =>
      context.child(gameId.value) match {
        case Some(game) => game forward command
        case None => sender() ! GameDoesNotExist
      }
  }

}
