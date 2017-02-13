name := "multi-relay"

import Versions._

libraryDependencies ++= Seq(
  "ch.qos.logback"     % "logback-classic"    % logbackVersion      % "provided",
  "org.slf4j"          % "slf4j-api"          % slf4jVersion        % "provided",
  "com.typesafe"      %% "scalalogging-slf4j" % scalaloggingVersion % "provided",
  "com.typesafe.akka" %% "akka-actor"         % akkaVersion         % "provided",
  "com.typesafe.akka" %% "akka-remote"        % akkaVersion         % "provided",
  "com.ubeeko"        %% "hfactory-util"      % hfactoryVersion     % "provided",
  "org.specs2"        %% "specs2-core"        % specs2Version       % "test"
)

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

