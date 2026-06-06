package com.slopezerosolutions.podmanutil

import upickle.default.ReadWriter
import upickle.default.macroRW

case class PodmanContainer(name: String, status: String, ports: String, internalIdOption: Option[Long] = None)

object PodmanContainer {
  implicit val rw: ReadWriter[PodmanContainer] = macroRW
}
