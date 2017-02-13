name := "multi-app"

import Versions._

libraryDependencies ++= Seq(
  "ch.qos.logback"      % "logback-classic"    % logbackVersion      % "provided",
  "org.slf4j"           % "slf4j-api"          % slf4jVersion        % "provided",
  "com.typesafe"       %% "scalalogging-slf4j" % scalaloggingVersion % "provided",
  "com.ubeeko"         %% "hfactory-app"       % hfactoryVersion     % "provided",
  "com.ubeeko"         %% "hfactory-core"      % hfactoryVersion     % "provided",
  "com.ubeeko"         %% "hfactory-jobs"      % hfactoryVersion     % "provided",
  "org.specs2"         %% "specs2-core"        % specs2Version       % "test",
  "org.scalatest"       % "scalatest_2.10"     % scalatestVersion    % "test"
)

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
