package lgasior.dicegame.api

import akka.actor.{ActorRef, Props, Actor}

object GameApiServiceActor {
  def props(gameManager: ActorRef) = Props(new GameApiServiceActor(gameManager))
}

class GameApiServiceActor(
    override val gameManager: ActorRef)
  extends Actor
  with GameApi {

  override val actorRefFactory = context

  override def receive = runRoute(gameRoute)

}
