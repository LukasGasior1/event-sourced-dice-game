package lgasior.dicegame.statistics.config

import com.typesafe.config.ConfigFactory

object Config {

  private val config = ConfigFactory.load()

  object Api {
    private val apiConfig = config.getConfig("api")

    val bindHost = apiConfig.getString("bind.host")
    val bindPort = apiConfig.getInt("bind.port")
  }

  object Events {
    private val eventsConfig = config.getConfig("events")

    val exchangeName = eventsConfig.getString("exchangeName")
    val queueName = eventsConfig.getString("queueName")
  }

}
