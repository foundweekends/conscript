package conscript

import scala.util.control.Exception.{catching,allCatch}
import java.io.File

object Apply extends Launch {

  def scriptFile(name: String) = windows map { _ =>
      homedir(("bin" / "%s.bat") format name)
    } getOrElse { homedir("bin" / name) }

  def exec(script: String) = {
    scala.sys.process.Process(windows.map { _ =>
      """cmd /c "%s" --version"""
    }.getOrElse {
      "%s --version"
    }.format(script))!
  }

  def config(user: String, repo: String, name: String, launch: Launchconfig) = {
    val launchconfig = configdir(user / repo / name / "launchconfig")

    val place = scriptFile(name)
    write(launchconfig, (launch update ConfigBootDir(forceslash(bootdir.toString))).toString).orElse {
      write(place, script(launchconfig)) orElse {
        allCatch.opt {
          place.setExecutable(true)
        }.filter { _ == true } match {
          case None => Some("Unable set as executable: " + place)
          case _ => None
        }
      }
    }.toLeft {
      allCatch.opt {
        exec(place.toString)
      } // ignore result status; the app might not have `--version`
      "Conscripted %s/%s to %s".format(user, repo, place)  
    }
  }

  def bootdir = configdir("boot")
  def write(file: File, body: String) =
    mkdir(file) orElse {
      catching(classOf[java.io.IOException]).either {
        val fw = new java.io.FileWriter(file)
        fw.write(body)
        fw.close()
      }.left.toOption.map { e => "Error writing " + file }
   }

  val javaopt = "-Xmx1G"
  def script(launchconfig: File) = windows map { _ =>
    """@echo off""" + "\r\n" +
    ("""java %%CONSCRIPT_OPTS%% %s -jar "%s" "@file://%s" %%*""" + "\r\n") format (javaopt, configdir(sbtlaunchalias),
      forceslash(launchconfig.getCanonicalPath))
  } getOrElse {
    """#!/bin/sh
      |java $CONSCRIPT_OPTS %s -jar %s @%s "$@"
      |""" .stripMargin format (javaopt, configdir(sbtlaunchalias), launchconfig.getCanonicalPath)
  }
}
