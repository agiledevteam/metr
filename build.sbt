import AssemblyKeys._

name := "metr"

version := "1.0"

scalaVersion := "2.10.2"

retrieveManaged := true

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.15.1"

libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.1"

resolvers += Resolver.sonatypeRepo("public")

scalacOptions ++= Seq("-feature", "-deprecation")

mainClass in assembly := Some("com.lge.metr.AppMain")

