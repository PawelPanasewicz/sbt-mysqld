import sbt._
import Keys._

object Build extends Build {

  lazy val sbtMysqldProject = Project("sbt-mysqld", file("."))
    .settings(
    version := "0.1-SNAPSHOT",
    organization := "pl.panasoft",
    organizationName := "pl.panasoft",
    scalaVersion := "2.10.3",
    resolvers ++= commonResolvers,
    libraryDependencies ++= List(
      "pl.panasoft" %% "pimps" % "0.1-SNAPSHOT",
      "pl.panasoft" %% "mysql-routines" % "0.1-SNAPSHOT"
    ),
    sbtPlugin := true
  )
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  val commonResolvers = List(
    "Sonatype repository releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "Maven central repository" at "http://repo1.maven.org/maven2/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe repository ivy-releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
    Resolver.url("Typesafe repository ivy pattern", url("http://repo.typesafe.com/typesafe/releases/"))(Resolver.ivyStylePatterns)
  )
}


