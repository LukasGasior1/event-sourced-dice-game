package lgasior.dicegame.statistics.api

import akka.actor.{Actor, ActorRef, Props}

object StatsApiServiceActor {
  def props(statsActor: ActorRef) = Props(new StatsApiServiceActor(statsActor))
}

class StatsApiServiceActor(override val statsActor: ActorRef) extends Actor with StatsApi {

  override val actorRefFactory = context

  override def receive = runRoute(statsRoute)

}
