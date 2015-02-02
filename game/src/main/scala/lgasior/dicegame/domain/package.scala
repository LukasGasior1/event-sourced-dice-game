package lgasior.dicegame

import scala.language.implicitConversions

package object domain {
  implicit def gameToRight(game: Game) = Right(game)
  implicit def violationToLeft(violation: GameRulesViolation) = Left(violation)

  type GameOrViolation = Either[GameRulesViolation, Game]
}
