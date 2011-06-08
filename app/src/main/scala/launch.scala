package conscript

import dispatch._
import java.io.{FileOutputStream, File}
import util.control.Exception._

trait Launch {
  val sbtversion = "0.7.7"
  val sbtlaunchalias = "sbt-launch.jar"

  def launchJar: Either[String, File] = configdir("sbt-launch-%s.jar" format sbtversion) match {
    case jar if jar.exists => Right(jar)
    case jar =>
      try {
        println("Fetching launcher...")
        val launchalias = configdir(sbtlaunchalias)
        if (!launchalias.getParentFile.exists) mkdir(launchalias)
        else ()

        val req = windows map { _ =>
          // sbt-launch 0.7.7 has a bug: https://github.com/harrah/xsbt/pull/38
          url("https://github.com/downloads/eed3si9n/xsbt/sbt-launch-0.7-SNAPSHOT.jar")
        } getOrElse{url("https://simple-build-tool.googlecode.com/files/sbt-launch-%s.jar" format sbtversion)}

        http(req >>> new FileOutputStream(jar))
        windows map { _ =>
          if (launchalias.exists) launchalias.delete
          else ()

          http(req >>> new FileOutputStream(launchalias))
        } getOrElse {
          val rt = Runtime.getRuntime
          rt.exec("ln -sf %s %s" format (jar, launchalias)).waitFor
        }
        Right(jar)
      } catch {
        case e: Exception => Left("Error downloading sbt-launch-%s: %s" format (sbtversion, e.toString))
      }
  }

  def / (a: String, b: String) = a + File.separatorChar + b
  def forceslash(a: String) =
    windows map { _ =>
      "/" + (a replaceAll ("""\\""", """/"""))
    } getOrElse {a}
  def configdir(path: String) = homedir(/(".conscript", path))
  def homedir(path: String) = new File(System.getProperty("user.home"), path)
  def mkdir(file: File) =
    catching(classOf[SecurityException]).either {
      new File(file.getParent).mkdirs()
    }.left.toOption.map { e => "Unable to create path " + file }
  def windows =
    System.getProperty("os.name") match {
      case x: String if x contains "Windows" => Some(x)
      case _ => None
    }
  val http = new Http with NoLogging
}
