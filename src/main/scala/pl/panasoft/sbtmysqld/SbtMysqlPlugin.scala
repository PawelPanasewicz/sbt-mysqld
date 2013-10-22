package pl.panasoft.sbtmysqld


import java.net.InetSocketAddress
import sbt._
import sbt.Keys._
import scala.sys.process.{ProcessLogger, Process}
import scala.None
import scala.Some
import sbt.Scoped.RichTaskable2
import pl.panasoft.mysqlroutines.MysqldRoutines
import scala.Some

object SbtMysqlPlugin {

  val mysqldStart = TaskKey[Unit]("mysqld-start", "")
  val mysqldParams = SettingKey[List[String]]("mysqld-params", "Params passed to mysqld.exe when starting mysqld (for example --port 3600)")
  val mysqldStop = TaskKey[Unit]("mysqld-stop", "")
  val mysqldRestart = TaskKey[Unit]("mysqld-restart", "")
  val mysqldCheck = TaskKey[Unit]("mysqld-check", "Check status of running mysqld.")
  val mysqlRestore = TaskKey[Unit]("mysql-restore", "Copies previously backuped mysq data dir.")
  val mysqlDataDir = SettingKey[File]("mysql-data-dir", "")
  val mysqlBootstrapDataDir = SettingKey[File]("mysql-bootstrap-data-dir", "")
  val mysqlHome = SettingKey[Option[File]]("mysql-home", "Installation directory of mysql. Default None. Provide this setting in your build definition.")
  val mysqlInstallStandalone = TaskKey[Unit]("mysql-install-standalone", "Downloads the standalone version of mysql and install it into 'mysqlHome'.")

  def mySqlSettings: Seq[Project.Setting[_]] =
    Seq(
      mysqlHome := None,
      mysqldParams := Nil,
      mysqlInstallStandalone <<= (mysqlHome) map {
        case Some(mysqlHome_) => installMySql(mysqlHome_)
        case None => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqldStart <<= (mysqlHome, mysqldParams in mysqldStart, streams) map {
        case (Some(mysqlHome_), mysqldParams: List[String], streams: TaskStreams) => MysqldRoutines.start(mysqlHome = mysqlHome_, mysqldParams = mysqldParams, fOut = streams.log.info(_), fErr = streams.log.error(_))
        case (None, _, streams: TaskStreams) => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqldCheck  <<= (mysqlHome, streams) map {
        case (Some(mysqlHome_), streams: TaskStreams) => MysqldRoutines.check(mysqlHome = mysqlHome_, fOut = streams.log.info(_), fErr = streams.log.error(_))
        case (None,streams: TaskStreams) => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqldStop <<= (mysqlHome, streams) map {
        case (Some(mysqlHome_), streams: TaskStreams) => MysqldRoutines.stop(mysqlHome = mysqlHome_, fOut = streams.log.info(_), fErr = streams.log.error(_))
        case (None, streams: TaskStreams) => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqldRestart <<= (mysqlHome, mysqldParams in mysqldStart, streams) map {
        case (Some(mysqlHome_), mysqldParams: List[String], streams: TaskStreams) => MysqldRoutines.restart(mysqlHome = mysqlHome_, mysqldParams = mysqldParams, fOut = streams.log.info(_), fErr = streams.log.error(_))
        case (None, _, streams: TaskStreams) => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqlDataDir <<= mysqlHome {
        case Some(mysqlHome) => mysqlHome / "data"
        case None => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqlBootstrapDataDir <<= mysqlHome {
        case Some(mysqlHome) => mysqlHome / "bootstrapDataDir"
        case None => throw SbtMysqlPluginException("The 'mysqlHome' is not defined.")
      },
      mysqlRestore <<= (mysqlDataDir, mysqlBootstrapDataDir) map {
        case (mysqlDataDir, mysqlBootstrapDataDir)=> MysqldRoutines.restoreDataDir(dataDir = mysqlDataDir, bootstrapDataDir = mysqlBootstrapDataDir)
      }
    )

  def installMySql(mysqlHome:File) =
    IO.withTemporaryFile("mysql-noinstall-5.1.72-winx64", "zip") {
      case tempFile =>
        IO.download(new java.net.URL("""http://dev.mysql.com/get/Downloads/MySQL-5.1/mysql-noinstall-5.1.72-winx64.zip/from/http://cdn.mysql.com/"""), tempFile)
        IO.delete(mysqlHome)
        IO.withTemporaryDirectory {
          tempDir =>
          IO.unzip(tempFile, tempDir)
          IO.copyDirectory( tempDir / "mysql-5.1.72-winx64", mysqlHome)
    }
  }
}

class SbtMysqlPluginException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)

object SbtMysqlPluginException {
  def apply(m: String, c: Throwable = null) = new SbtMysqlPluginException(message = m, cause = c)
}

