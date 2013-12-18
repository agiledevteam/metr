name := "metr"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Maven Repository for Spoon" at "http://spoon.gforge.inria.fr/repositories/releases/"

retrieveManaged := true

libraryDependencies += "fr.inria.gforge.spoon" % "spoon-core" % "1.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.15.1"

resolvers += Resolver.sonatypeRepo("public")

scalacOptions ++= Seq("-feature", "-deprecation")
