package lgasior.dicegame.statistics.actor

import akka.actor.Props
import akka.persistence.PersistentActor
import lgasior.dicegame.statistics.Stats

object StatsActor {
  def props = Props[StatsActor]

  sealed trait Command
  case class IncRollsCount(rolledNumber: Int) extends Command
  case object GetState extends Command

  sealed trait Event
  case class RollsCountIncreased(rolledNumber: Int) extends Event
}

class StatsActor extends PersistentActor {

  import StatsActor._

  override val persistenceId = "rolls_stats"

  var state: Stats = Stats(Map.empty)

  def applyEvent(event: Event) = event match {
    case RollsCountIncreased(rolledNumber) =>
      state = state.incRollsCount(rolledNumber)
  }

  override def receiveCommand = {
    case IncRollsCount(rolledNumber) =>
      persist(RollsCountIncreased(rolledNumber))(applyEvent)
    case GetState =>
      sender() ! state
  }

  override def receiveRecover = {
    case ev: Event => applyEvent(ev)
  }

}
