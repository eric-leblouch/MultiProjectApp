package io.hfactory.multi.protocol

import java.util.UUID

/** Messages example to communicate between the server app and not defined streaming app. */

sealed trait StreamElement
object StreamElement {
  // XXX: initiator should be ActorRef.
  case class Marker(uuid: UUID, blockIndex: Int, blockLength: Int, initiator: String) extends StreamElement
  case class Tag(name: String, value: String) extends StreamElement
}

case class ProcessElements(elements: Seq[StreamElement])
