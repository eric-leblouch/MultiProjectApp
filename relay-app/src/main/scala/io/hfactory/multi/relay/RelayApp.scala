package io.hfactory.multi.relay

import akka.actor.ActorSystem

import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import com.typesafe.config.{Config => TypeSafeConfig}
import com.typesafe.scalalogging.slf4j.Logging

import com.ubeeko.commandline._
import com.ubeeko.config.Config

import scala.util.{Try, Failure, Success}

object CommandLine extends CommandLine {
  val conf      = opt[String](('s', "static-conf"), param = "resource file",
                              description = "Relay's static (Akka) configuration file.")
  val serveConf = mandatoryOpt[String](('c', "conf"), param = "file", description = "Example's configuration file.")
  val help      = helpFlag
}

object RelayApp extends Logging {
  val defaultConfigName = "ex-relay.conf"

  val relaySystemName = "RelaySystem"
  val relayActorName  = "RelayActor"

  def main(args: Array[String]): Unit =
    if (CommandLine.helpRequested(args))
      printUsageHelp()
    else
      CommandLine.parse(args) match {
        case Success(parseResult) => run(parseResult)
        case Failure(err) =>
          logger.error(err.getMessage)
          System.exit(1)
      }

  private def run(implicit parseResult: CommandLineParseResult): Unit = {
    logger.info(s"Setting up relay configuration...")

    val config = for (
      relayConfigName <- CommandLine.conf.valueOrDefault(defaultConfigName);
      serveConfigName <- CommandLine.serveConf.value;
      relayConfig     <- Try { ConfigFactory.load(relayConfigName) };
      serveConfig     <- Config.load(serveConfigName);
      hostname        <- serveConfig.tryGet[String]("ex.relay.hostname");
      port            <- serveConfig.tryGet[Int]("ex.relay.port")
    ) yield {
      relayConfig.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(hostname))
                 .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(port))
    }

    config match {
      case Success(akkaConfig) => startActorSystem(akkaConfig)
      case Failure(err) =>
    }
  }

  private def startActorSystem(akkaConfig: TypeSafeConfig): Unit = {
    logger.info("Starting relay actor system...")
    val relaySystem = ActorSystem(relaySystemName, akkaConfig)
    println(s"Relay actor system: $relaySystem")

    logger.info("Spawning relay actor...")
    val relayActor = relaySystem.actorOf(RelayActor.props, relayActorName)
    println(s"Relay actor: $relayActor")

    println("Ready.")
  }

  private def printUsageHelp(): Unit = {
    val self = Option(System.getenv("UBEEKO_SELF")).getOrElse(this.getClass.getSimpleName)
    val help = DefaultUsageHelpFormatter.format(
      programName = self,
      description = List("Relay between example's serve app and streaming app."),
      commandLine = CommandLine
    )
    println(help)
  }
}
