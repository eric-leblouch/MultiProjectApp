package io.hfactory.multi.app

import akka.actor.{ActorContext, ActorRef}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

object RelayLookup {
  private var actorContext: ActorContext = null
  private var relayHostname: String = null
  private var relayPort: Int = 0

  def init(actorContext: ActorContext, relayHostname: String, relayPort: Int): Unit = {
    this.actorContext = actorContext
    this.relayHostname = relayHostname
    this.relayPort = relayPort
  }

  def apply(): Future[ActorRef] = {
    val relayActorName = s"akka.tcp://RelaySystem@$relayHostname:$relayPort/user/RelayActor"
    implicit val resolveTimeout = Timeout(5.seconds)
    actorContext.actorSelection(relayActorName).resolveOne()
  }
}
