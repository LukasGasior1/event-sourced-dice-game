package lgasior.dicegame.actor

import akka.actor.{Cancellable, ActorLogging, Props}
import akka.persistence.{RecoveryCompleted, PersistentActor}

import lgasior.dicegame.domain._

import scala.concurrent.duration._

object GameActor {
  def props(id: GameId) = Props(new GameActor(id))

  sealed trait CommandResult
  case object CommandAccepted extends CommandResult
  case class CommandRejected(violation: GameRulesViolation) extends CommandResult

  private case object TickCountdown
}

class GameActor(id: GameId) extends PersistentActor with ActorLogging {

  import GameActor._
  import context.{dispatcher, system}

  override val persistenceId = id.value

  var game: Game = Game.create(id)

  var tickCancellable: Option[Cancellable] = None

  override def receiveCommand = {
    case command: GameCommand => handleResult(game.handleCommand(command))
    case TickCountdown => game match {
      case rg: RunningGame => handleChanges(rg.tickCountdown())
      case _ =>
        log.warning("Game is not running, cannot update countdown")
        cancelCountdownTick()
    }
  }

  def handleResult(result: Either[GameRulesViolation, Game]) = result match {
    case Right(updatedGame) =>
      sender() ! CommandAccepted
      handleChanges(updatedGame)
    case Left(violation) =>
      sender() ! CommandRejected(violation)
  }

  def handleChanges(updatedGame: Game) =
    updatedGame.uncommittedEvents.foreach {
      persist(_) { ev =>
        game = game.applyEvent(ev).markCommitted
        publishEvent(ev)
        ev match {
          case _: GameStarted =>
            scheduleCountdownTick()
          case _: TurnChanged =>
            cancelCountdownTick()
            scheduleCountdownTick()
          case _: GameFinished =>
            cancelCountdownTick()
            context stop self
          case _ => // nothing
        }
      }
    }

  def publishEvent(event: GameEvent) = {
    system.eventStream.publish(event)
  }

  def scheduleCountdownTick() = {
    val cancellable =
      system.scheduler.schedule(1.second, 1.seconds, self, TickCountdown)
    tickCancellable = Some(cancellable)
  }

  def cancelCountdownTick() = {
    tickCancellable.foreach(_.cancel())
    tickCancellable = None
  }

  override def receiveRecover = {
    case ev: GameEvent =>
      game = game.applyEvent(ev)
    case RecoveryCompleted =>
      if (game.isRunning)
        scheduleCountdownTick()
  }

}
