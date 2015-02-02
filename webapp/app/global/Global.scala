package global

import io.scalac.amqp.Connection
import play.api.GlobalSettings

object Global extends GlobalSettings {
  lazy val connection = Connection()
}
