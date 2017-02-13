# MultiProjectApp

## Modules

### Datamodel

The datamodel module defines the entities that will be used in all other modules.

### Protocol

The protocol module defines a protocol to talk with a Relay actor. It depends on the datamodel to be able to pass entities between applications.

### App

The app module is the HFactory application and depends on the datamodel and optionnally on the protocol to talk to the relay application. It can be deployed locally using the local_install.sh script.

### Relay

The relay application allows to pass information between the different application defined. It could be used to push information in a streaming application. This application depends on the protocol and the datamodel modules and is an Akka Actor.

## Configuration

### Versions

To factorize all the versions in one place. A `versions.scala` file can be found in the `project` folder and all build.sbt are importing `Versions._`.

### Scala version and plugins

This project uses Scala 2.10.6 with the macro paradise plugin 2.1.0-M5 and sbt-assembly 0.13.0.

## Compilation

Simply use `sbt compile`, `sbt test` and `sbt assembly` to compile, test or create the jars.

You can then use local_install.sh to locally install the application that can then be installed on your remote server by replacing the remote folder with your local one.

