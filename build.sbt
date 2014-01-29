import AssemblyKeys._

name := "metr"

version := "1.0"

scalaVersion := "2.10.2"

retrieveManaged := true

antlr4Settings

antlr4PackageName in Antlr4 := Some("com.lge.metr")

antlr4GenListener in Antlr4 := true // default: true

antlr4GenVisitor in Antlr4 := true // default: false

libraryDependencies ++= List(
    "com.typesafe.slick" %% "slick" % "2.0.0",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.h2database" % "h2" % "1.3.170"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

libraryDependencies += "com.netflix.rxjava" % "rxjava-scala" % "0.15.1"

libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.1"

libraryDependencies += "org.parboiled" % "parboiled-scala_2.10" % "1.1.5"

libraryDependencies += "org.parboiled" % "parboiled-java" % "1.1.6"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.2.0.201312181205-r"

resolvers += Resolver.sonatypeRepo("public")

scalacOptions ++= Seq("-feature", "-deprecation")

mainClass in assembly := Some("com.lge.metr.AppMain")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

