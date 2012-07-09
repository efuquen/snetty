import sbt._
import sbt.Keys._

object SnettyBuild extends Build {

  lazy val snetty = Project(
    id = "snetty",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "snetty",
      organization := "com.edftwin",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" % "akka-actor" % "2.0.1",
        "org.jboss.netty" % "netty" % "3.2.7.Final"
      )
    )
  )
}
