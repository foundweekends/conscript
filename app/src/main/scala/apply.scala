package conscript

import scala.util.control.Exception.{catching,allCatch}
import java.io.File

object Apply {
  def config(user: String, repo: String, name: String, launch: String) = {
    val launchconfig = configdir(/(/(/(user, repo), name), "launchconfig"))

    val place = homedir(/("bin", name))
    write(launchconfig, launch + boot).orElse {
      write(place, script(launchconfig)) orElse {
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
  def script(launchconfig: File) = 
    """#!/bin/sh
      |java -jar %s @%s "$@"
      |""" .stripMargin.
        format(configdir("sbt-launch.jar"), launchconfig.getCanonicalPath)
  val boot = """
            |[boot]
            |  directory: %s
            |""".stripMargin.format(configdir("boot"))
}
