package io.hfactory.multi.relay

import io.hfactory.multi.protocol.StreamElement
import io.hfactory.multi.relay.RelayProtocol.Wart

import java.util.UUID

import org.specs2.mutable._

class RelayWartSpec extends Specification {
  def identity(elem: StreamElement) = Wart.parseElement(Wart.formatElement(elem))

  "Wart.parseElement o art.formatElement" should {
    "be identity" in {
      val tagElement    = StreamElement.Tag("name", "my name")
      val markerElement = StreamElement.Marker(UUID.randomUUID(), blockIndex = 3, blockLength = 18,
                                               initiator = "some_actor_path")

      identity(tagElement) must_== tagElement
      identity(markerElement) must_== markerElement
    }
  }
}
