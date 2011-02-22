package conscript

import scala.util.control.Exception.catching
import java.io.File

object Apply {
  def config(user: String, repo: String, launch: String, props: Option[String]) = {
    val launchconfig = configdir(/(user, repo))
    write(launchconfig, launch + boot).toLeft {
      "Wrote " + launchconfig
    }
  }
  def / (a: String, b: String) = a + File.separatorChar + b
  def configdir(path: String) =
    new File(System.getProperty("user.home"), /(".conscript", path))
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
  def script(options: String) = 
    """#!/bin/sh
      |java -jar ~/.conscript/sbt-launch.jar %s "$@"""".stripMargin.format(options)
  val boot = """
            |[boot]
            |  directory: %s
            |""".stripMargin.format(configdir("boot"))
}
