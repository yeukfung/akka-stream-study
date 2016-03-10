name := """akka-stream-study"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.3"
libraryDependencies += "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "2.0.3"
libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.0.3"

