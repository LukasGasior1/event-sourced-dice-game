package lgasior.dicegame.domain

import java.util.UUID

object GameId {
  def createRandom = GameId(UUID.randomUUID().toString)
}

case class GameId(override val value: String) extends AnyVal with Id[Game]

