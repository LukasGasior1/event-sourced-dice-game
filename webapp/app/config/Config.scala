package config

import com.typesafe.config.ConfigFactory

object Config {

  private val config = ConfigFactory.load()

  object Events {
    private val eventsConfig = config.getConfig("events")

    val exchangeName = eventsConfig.getString("exchangeName")
  }

  object Game {
    private val gameConfig = config.getConfig("game")

    val apiUrl = gameConfig.getString("apiUrl")
  }

}
