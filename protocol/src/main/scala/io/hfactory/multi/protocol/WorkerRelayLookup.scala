package io.hfactory.multi.protocol

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout

import java.net.InetAddress

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.Future

object WorkerRelayLookup {
  private val hostname = InetAddress.getLocalHost.getHostName
  private val config = ConfigFactory.parseString(
    s"""akka {
       |  loglevel = "INFO"
       |  actor {
       |    provider = "akka.remote.RemoteActorRefProvider"
       |  }
       |  remote {
       |    entabled-transports = ["akka.remote.netty.tcp"]
       |    netty.tcp {
       |      hostname = "${hostname}"
       |      port = 0
       |    }
       |  }
       |}""".stripMargin
  )
  private val actorSystem: ActorSystem = ActorSystem(s"local_${hostname}", config.withFallback(ConfigFactory.load()))
  private var relayHostname: String = null
  private var relayPort: Int = 0

  def init(relayHostname: String, relayPort: Int): Unit = {
    this.relayHostname = relayHostname
    this.relayPort = relayPort
  }

  def apply(): Future[ActorRef] = {
    val relayActorName = s"akka.tcp://RelaySystem@$relayHostname:$relayPort/user/RelayActor"
    implicit val resolveTimeout = Timeout(5.seconds)
    actorSystem.actorSelection(relayActorName).resolveOne()
  }
}
