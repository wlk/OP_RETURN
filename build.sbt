import AssemblyKeys._

assemblySettings

name := "blockchain-message"

version := "1.0"

jarName in assembly := "blockchain-message.jar"

test in assembly := {}

organization := "com.wlangiewicz"

version := "1.0"

scalaVersion := "2.11.2"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome+"/.m2/repository"

libraryDependencies += "org.scala-sbt" % "command" % "0.13.6"

//libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.12"

libraryDependencies += "org.bitcoinj" % "bitcoinj-core" % "0.13-SNAPSHOT"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.7"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
