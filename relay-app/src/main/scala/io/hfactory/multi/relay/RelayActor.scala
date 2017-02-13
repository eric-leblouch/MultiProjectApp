package io.hfactory.multi.relay

import java.util.UUID

import akka.actor.{ActorRef, Props, Actor}

import com.typesafe.scalalogging.slf4j.Logging

import io.hfactory.multi.protocol.{StreamElement, ProcessElements}

import scala.util.control.NonFatal

/** Actor relaying tags it receives to an actor.
  *
  * The actor must register itself by sending message
  * `ActorIdentity`.
  *
  * When receiving tags, the relay actor will forward them to the
  * configured actor or drop them (with a warning) if none is
  * configured.
  */
class RelayActor extends Actor with Logging {
  import RelayProtocol._

  private var actor: Option[ActorRef] = None

  def receive: Receive = {
    case ActorIdentity(newRef) =>
      val msg = actor match {
        case Some(oldRef) => s"Relaying to actor $newRef in place of $oldRef"
        case None         => s"Relaying to actor $newRef"
      }
      logger.info(msg)
      actor = Some(newRef)

    case msg @ ProcessElements(elements) =>
      // XXX Wart! for some unknown reason the StreamActor doesn't receive the
      // strongly-typed message, so we have to send List[String] instead.
      val csvMsg = elements.map(Wart.formatElement).toList
      inject(csvMsg)

    case x =>
      logger.warn(s"Received unsupported message: $x")
  }

  private def inject(msg: Any): Unit =
    actor match {
      case Some(ref) =>
        //logger.debug(s"Forwarding message $msg to actor: $ref")
        ref ! msg
      case None =>
        logger.warn(s"No actor configured, dropping message $msg")
    }
}

object RelayActor {
  def props: Props = Props[RelayActor]
}

object RelayProtocol {
  case class ActorIdentity(ref: ActorRef)

  object Wart {
    def formatElement(e: StreamElement): String = e match {
      case m: StreamElement.Marker => s"M|${m.uuid},${m.blockIndex},${m.blockLength},${m.initiator}"
      case v: StreamElement.Tag  => s"T|${v.name}:${v.value}"
    }

    def parseElement(s: String): StreamElement = {
      if (s.startsWith("M|")) {
        try {
          val List(uuid, blockIndex, blockLength, initiatorPath) = s.drop(2).split(",").toList
          StreamElement.Marker(UUID.fromString(uuid), blockIndex.toInt, blockLength.toInt, initiatorPath)
        } catch {
          case NonFatal(_) => throw new IllegalArgumentException(s"Invalid marker element: [[[$s]]]")
        }
      } else if (s.startsWith("T|")) {
        val name :: value :: Nil = s.drop(2).split(":").toList
        StreamElement.Tag(name, value)
      } else
        throw new IllegalArgumentException(s"Invalid stream element: [[[$s]]]")
    }
  }
}
