name := "multi-datamodel"

import Versions._

libraryDependencies ++= Seq(
  "ch.qos.logback"  % "logback-classic"    % logbackVersion      % "provided",
  "org.slf4j"       % "slf4j-api"          % slf4jVersion        % "provided",
  "com.typesafe"   %% "scalalogging-slf4j" % scalaloggingVersion % "provided",
  "com.ubeeko"     %% "hfactory-core"      % hfactoryVersion     % "provided",
  "org.specs2"     %% "specs2-core"        % specs2Version       % "test"
)
// "com.ubeeko"     %% "htalk"              % htalkVersion        % "provided" exclude("junit","junit"),

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

