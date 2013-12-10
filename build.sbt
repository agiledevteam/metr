name := "metr"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Maven Repository for Spoon" at "http://spoon.gforge.inria.fr/repositories/releases/"

libraryDependencies ++= Seq(
  "fr.inria.gforge.spoon" % "spoon-core" % "1.5"
)
