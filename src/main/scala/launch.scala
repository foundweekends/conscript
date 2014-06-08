package conscript

import conscript.BuildInfo.sbtVersion
import dispatch._
import java.io.{FileOutputStream, File}
import util.control.Exception._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait Launch extends Credentials {
  import Conscript.http

  val sbtlaunchalias = "sbt-launch.jar"

  def launchJar(display: Display): Either[String, String] =
      configdir("sbt-launch-%s.jar" format sbtVersion) match {
    case jar if jar.exists => Right("Launcher already up to date, fetching next...")
    case jar =>
      try {
        display.info("Fetching launcher...")
        val launchalias = configdir(sbtlaunchalias)
        if (!launchalias.getParentFile.exists) mkdir(launchalias)
        else ()

        val req = url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/%s/sbt-launch.jar" format sbtVersion)

        val future = for {
          _ <- http(req > as.File(jar))
          _ <- windows.map { _ =>
            if (launchalias.exists) launchalias.delete
            else ()
            // should copy the one we already downloaded, but I don't
            // have a windows box to test any changes
            http(req > as.File(launchalias))
          } getOrElse {
            val rt = Runtime.getRuntime
            Future.successful(rt.exec("ln -sf %s %s" format (jar, launchalias)).waitFor)
          }
        } yield Right("Fetching Conscript...")

        Await.result(future, 30.seconds)
      } catch {
        case e: Exception => 
          Left("Error downloading sbt-launch-%s: %s".format(
            sbtVersion, e.toString
          ))
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
}
