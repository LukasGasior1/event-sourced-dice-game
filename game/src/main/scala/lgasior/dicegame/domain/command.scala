package lgasior.dicegame.domain

sealed trait GameCommand
case class StartGame(players: Seq[PlayerId]) extends GameCommand
case class RollDice(player: PlayerId) extends GameCommand
