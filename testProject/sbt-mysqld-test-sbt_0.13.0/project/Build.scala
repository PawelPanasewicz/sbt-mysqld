import sbt._
import Keys._
import pl.panasoft.sbtmysqld.SbtMysqlPlugin._

object Build extends Build {

  lazy val sbtMysqldProject = Project("sbt-mysqld-test", file("."))
    .settings(pl.panasoft.sbtmysqld.SbtMysqlPlugin.mySqlSettings: _*)
    .settings(
    version := "0.1-SNAPSHOT",
    organization := "pl.panasoft",
    organizationName := "pl.panasoft",
    scalaVersion := "2.10.3",
    mysqlHome := Some(file("mysql_home"))
  )

    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}

