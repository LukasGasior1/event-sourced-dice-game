package lgasior.dicegame.domain

import lgasior.dicegame.config.Config.Game._
import org.scalatest.FeatureSpec
import org.scalatest.Matchers

class GameSpec extends FeatureSpec with Matchers {

  val gameId = GameId.createRandom

  feature("dice roll") {

    val previousPlayer = PlayerId("1")
    val currentPlayer = PlayerId("2")
    val nextPlayer = PlayerId("3")

    val game = RunningGame(
      id = gameId,
      players = Seq(previousPlayer, currentPlayer, nextPlayer),
      turn = Turn(currentPlayer, 30),
      Map(previousPlayer -> 2))

    scenario("current player roll") {
      val res = game.roll(currentPlayer)

      res shouldBe 'right
      res.right.foreach { updatedGame =>
        val runningGame = updatedGame.asInstanceOf[RunningGame]
        runningGame.rolledNumbers should contain key currentPlayer
        runningGame.turn.currentPlayer shouldBe nextPlayer
        runningGame.turn.secondsLeft shouldBe turnTimeoutSeconds
        runningGame.uncommittedEvents should contain (TurnChanged(gameId, Turn(nextPlayer, turnTimeoutSeconds)))
      }
    }

    scenario("not current player roll") {
      val res = game.roll(nextPlayer)

      res shouldBe Left(NotCurrentPlayerViolation)
    }

    scenario("last player roll") {
      val gameWithoutNextPlayer = game.copy(players = game.players.take(2))
      val res = gameWithoutNextPlayer.roll(currentPlayer)

      res shouldBe 'right
    }

  }

}
