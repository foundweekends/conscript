package conscript

import dispatch._
import java.io.{FileOutputStream, File}
import util.control.Exception._

trait Launch {
  val sbtversion = "0.10.0"
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
          // XXX need a 0.10.0 jar for windows peeps
          url("https://github.com/downloads/eed3si9n/xsbt/sbt-launch-0.7-SNAPSHOT.jar")
        } getOrElse{url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-tools.sbt/sbt-launch/%s/sbt-launch.jar" format sbtversion)}

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

  implicit def str2paths(a: String) = new {
    def / (b: String) = a + File.separatorChar + b
  }
  def forceslash(a: String) =
    windows map { _ =>
      "/" + (a replaceAll ("""\\""", """/"""))
    } getOrElse {a}
  def configdir(path: String) = homedir(".conscript" / path)
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
