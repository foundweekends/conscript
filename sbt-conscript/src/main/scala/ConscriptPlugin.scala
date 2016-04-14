package sbtconscript

import sbt._
import Keys._

object ConscriptPlugin extends AutoPlugin {
  lazy val conscriptStr = "conscript"
  override lazy val requires = plugins.JvmPlugin
  object autoImport {
    lazy val csBoot = settingKey[File]("Boot directory used by csRun")
    lazy val csWrite = taskKey[Unit]("Write test launchconfig files to conscript-output")
    lazy val csRun = inputKey[Unit]("Run a named launchconfig, with parameters")
  }

  import autoImport._
  override def projectSettings: Seq[Def.Setting[_]] =
    List(
      libraryDependencies += "org.scala-sbt" % "launcher" % "1.0.0" % "provided",
      sourceDirectory in csRun := { (sourceDirectory in Compile).value / conscriptStr },
      target in csRun := { target.value / conscriptStr },
      csBoot := { (target in csRun).value / "boot" },
      csWrite := csWriteTask.value,
      csRun := csRunTask.evaluated,
      (aggregate in csRun) := false
    )

  private def configs(path: File) = (path ** "launchconfig").get
  private def configName(path: File) = file(path.getParent).getName
  lazy val csWriteTask = Def.task {
    val base = (sourceDirectory in csRun).value
    val output = (target in csRun).value
    val boot = csBoot.value
    IO.delete(output)
    IO.copyDirectory(base, output)
    configs(output).map { path =>
      IO.append(path,
        """
        |[boot]
        |  directory: %s
        |""".stripMargin.format(boot)
      )
    }
  }
  lazy val csRunTask = Def.inputTask {
    import sbt.Process._
    val args = Def.spaceDelimited().parsed
    val x = csWrite.value
    val y = publishLocal.value
    val output = (target in csRun).value
    val config = args.headOption.map { name =>
      configs(output).find {
        p => configName(p) == name
      }.getOrElse { sys.error("No launchconfig found for " + name) }
    }.getOrElse { sys.error("Usage: cs-run <appname> [args ...]") }
    "sbt @%s %s".format(config,
                        args.toList.tail.mkString(" ")
    ) ! match {
      case 0 => ()
      case n => sys.error("Launched app error code: " + n)
    }
  }
}
