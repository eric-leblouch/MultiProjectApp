package io.hfactory.multi.app

import java.io.File

import com.ubeeko.hfactory.app.HApp
import io.hfactory.multi.protocol._
import java.util.Date

import net.liftweb.json._

import scala.concurrent.duration._
import spray.http._

class MultiApp extends HApp {
  implicit val ec = appContext.actorContext.dispatcher

  sprayAlias("sensors", "Sensor")
  sprayAlias("systems", "System")
  sprayAlias("devices", "Device")

  val relayHostname = getSetting[String]("ex.relay.hostname")
  val relayPort     = getSetting[Int]   ("ex.relay.port")

  logger.debug(s"ex.relay.hostname: $relayHostname")
  logger.debug(s"ex.relay.port: $relayPort")

  RelayLookup.init(appContext.actorContext, relayHostname, relayPort)
}
