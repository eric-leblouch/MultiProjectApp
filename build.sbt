name := "Multi"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.6"

organization in ThisBuild := "io.hfactory"

// XXX Do we have to enable cached resolution for all sub-projects?
// The documentation is unclear (to say the least...)
lazy val commonSettings = Seq(
    updateOptions := updateOptions.value.withCachedResolution(true)
)
lazy val root = project
    .in(file("."))
    .disablePlugins(AssemblyPlugin)
    .settings(commonSettings:_*)
    .settings(
        publishArtifact := true,
        publish := {},
        publishLocal := {})
    .aggregate(datamodel, app, protocol, relay)

lazy val datamodel = project
  .settings(commonSettings:_*)

lazy val app = project
  .settings(commonSettings:_*)
  .dependsOn(datamodel, protocol)

lazy val relay = project
  .in(file("relay-app"))
  .settings(commonSettings:_*)
  .dependsOn(datamodel, protocol)

lazy val protocol = project
  .settings(commonSettings:_*)
  .dependsOn(datamodel)

resolvers in ThisBuild ++= Seq(
  "Ubeeko nexus public" at "http://nexus.hfactory.io/nexus/content/groups/public",
  "Ubeeko nexus releases" at "http://nexus.hfactory.io/nexus/content/repositories/releases/",
  "Ubeeko nexus snapshots" at "http://nexus.hfactory.io/nexus/content/repositories/snapshots/",
  Classpaths.typesafeReleases
)

scalacOptions in ThisBuild ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Xlog-implicits",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions in ThisBuild += Tests.Argument(TestFrameworks.JUnit, "-v")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
