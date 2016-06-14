name := "TwitterStreamer"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.7"

libraryDependencies += "com.hunorkovacs" %% "koauth" % "1.1.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion
libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"