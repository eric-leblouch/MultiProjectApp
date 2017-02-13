name := "multi-protocol"

import Versions._

libraryDependencies ++= Seq(
  "com.ubeeko" %% "hfactory-core" % hfactoryVersion % "provided",
  "com.ubeeko" %% "hfactory-util" % hfactoryVersion % "provided"
)

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

