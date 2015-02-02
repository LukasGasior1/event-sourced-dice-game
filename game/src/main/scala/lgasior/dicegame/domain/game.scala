package lgasior.dicegame.domain

import lgasior.dicegame.config.Config.Game._

import scala.util.Random

object Game {
  def create(id: GameId) = UninitializedGame(id)
}

sealed trait Game extends AggregateRoot[Game, GameEvent] {
  def id: GameId

  def handleCommand(command: GameCommand): GameOrViolation = command match {
    case StartGame(players) => this match {
      case ug: UninitializedGame => ug.start(players)
      case _ => GameAlreadyStartedViolation
    }
    case RollDice(player) => this match {
      case rg: RunningGame => rg.roll(player)
      case _ => GameNotRunningViolation
    }
  }

  def isFinished = this match {
    case fg: FinishedGame => true
    case _ => false
  }

  def isRunning = this match {
    case rg: RunningGame => true
    case _ => false
  }

}

case class UninitializedGame(
    override val id: GameId,
    override val uncommittedEvents: List[GameEvent] = Nil)
  extends Game {

  def start(players: Seq[PlayerId]): GameOrViolation =
    if (players.size < 2)
      NotEnoughPlayersViolation
    else {
      val firstPlayer = players.head
      applyEvents(GameStarted(id, players, Turn(firstPlayer, turnTimeoutSeconds)))
    }

  override def applyEvent = {
    case ev @ GameStarted(_, players, initialTurn) =>
      RunningGame(id, players, initialTurn,
        uncommittedEvents = uncommittedEvents :+ ev)
  }

  override def markCommitted = copy(uncommittedEvents = Nil)
}

sealed trait InitializedGame extends Game {
  def players: Seq[PlayerId]
}

case class Turn(currentPlayer: PlayerId, secondsLeft: Int)

case class RunningGame(
    override val id: GameId,
    override val players: Seq[PlayerId],
    turn: Turn,
    rolledNumbers: Map[PlayerId, Int] = Map.empty,
    override val uncommittedEvents: List[GameEvent] = Nil)
  extends InitializedGame {

  def roll(player: PlayerId): GameOrViolation = {
    if (turn.currentPlayer == player) {
      val rolledNumber = Random.nextInt(6) + 1
      val diceRolled = DiceRolled(id, rolledNumber)
      nextPlayerOpt match {
        case Some(nextPlayer) =>
          applyEvents(diceRolled, TurnChanged(id, Turn(nextPlayer, turnTimeoutSeconds)))
        case None =>
          applyEvent(diceRolled) match {
            case rg: RunningGame => rg.applyEvent(GameFinished(id, rg.bestPlayers))
            case other => other
          }
      }
    } else {
      NotCurrentPlayerViolation
    }
  }

  def bestPlayers: Set[PlayerId] = {
    val highest = highestRolledNumber
    rolledNumbers.collect { case (player, `highest`) => player }.toSet
  }

  def highestRolledNumber: Int =
    if (rolledNumbers.isEmpty)
      0
    else
      rolledNumbers.map(_._2).max

  def tickCountdown(): Game = {
    val countdownUpdated = TurnCountdownUpdated(id, turn.secondsLeft - 1)
    if (turn.secondsLeft <= 1) {
      val timedOut = TurnTimedOut(id)
      nextPlayerOpt match {
        case Some(nextPlayer) =>
          applyEvents(countdownUpdated, timedOut, TurnChanged(id, Turn(nextPlayer, turnTimeoutSeconds)))
        case None =>
          applyEvents(countdownUpdated, timedOut, GameFinished(id, bestPlayers))
      }
    } else applyEvent(countdownUpdated)
  }

  private def nextPlayerOpt: Option[PlayerId] = {
    val currentPlayerIndex = players.indexOf(turn.currentPlayer)
    val nextPlayerIndex = currentPlayerIndex + 1
    if (players.isDefinedAt(nextPlayerIndex))
      Some(players(nextPlayerIndex))
    else
      None
  }

  override def applyEvent = {
    case ev @ TurnChanged(_, newTurn) =>
      copy(turn = newTurn,
        uncommittedEvents = uncommittedEvents :+ ev)
    case ev @ DiceRolled(_, rolledNumber) =>
      copy(rolledNumbers = rolledNumbers + (turn.currentPlayer -> rolledNumber),
        uncommittedEvents = uncommittedEvents :+ ev)
    case ev @ TurnCountdownUpdated(_, secondsLeft) =>
      val updatedTurn = turn.copy(secondsLeft = secondsLeft)
      copy(turn = updatedTurn,
        uncommittedEvents = uncommittedEvents :+ ev)
    case ev @ GameFinished(_, winners) =>
      FinishedGame(id, players, winners,
        uncommittedEvents = uncommittedEvents :+ ev)
    case ev: TurnTimedOut =>
      copy(uncommittedEvents = uncommittedEvents :+ ev)
  }

  override def markCommitted = copy(uncommittedEvents = Nil)
}

case class FinishedGame(
    override val id: GameId,
    override val players: Seq[PlayerId],
    winners: Set[PlayerId],
    override val uncommittedEvents: List[GameEvent] = Nil)
  extends InitializedGame {

  override def applyEvent = PartialFunction.empty

  override def markCommitted = copy(uncommittedEvents = Nil)
}
