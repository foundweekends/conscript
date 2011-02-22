package conscript

import scala.util.control.Exception.{catching,allCatch}
import java.io.File

object Apply {
  def config(user: String, repo: String, launch: String, props: Map[String,String]) = {
    val launchconfig = configdir(/(user, repo))

    val name = props.getOrElse("executable", repo)
    val place = homedir(/("bin", name))
    val options = props.getOrElse("options", "")
    write(launchconfig, launch + boot).orElse {
      write(place, script(options, launchconfig)) orElse {
        allCatch.opt {
          place.asInstanceOf[
            { def setExecutable(b: Boolean): Boolean}
          ].setExecutable(true)
        }.filter { _ == true } match {
          case None => Some("Unable set as executable: " + place)
          case _ => None
        }
      }
    }.toLeft {
      "Conscripted %s/%s to %s".format(user, repo, place)
    }
  }
  def / (a: String, b: String) = a + File.separatorChar + b
  def configdir(path: String) =
    homedir(/(".conscript", path))
  def homedir(path: String) =
    new File(System.getProperty("user.home"), path)
  def write(file: File, body: String) =
    mkdir(file) orElse {
      catching(classOf[java.io.IOException]).either {
        val fw = new java.io.FileWriter(file)
        fw.write(body)
        fw.close()
      }.left.toOption.map { e => "Error writing " + file }
   }

  def mkdir(file: File) =
    catching(classOf[SecurityException]).either {
      new File(file.getParent).mkdirs()
    }.left.toOption.map { e => "Unable to create path " + file }
  def script(options: String, launchconfig: File) = 
    """#!/bin/sh
      |java -jar %s %s @%s "$@"
      |""" .stripMargin.
        format(configdir("sbt-launch.jar"), options, launchconfig.getCanonicalPath)
  val boot = """
            |[boot]
            |  directory: %s
            |""".stripMargin.format(configdir("boot"))
}
