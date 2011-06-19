package conscript

import sbt._
import Keys._

object Harness extends Plugin {
  val conscriptBase = SettingKey[File]("conscript-base")
  val conscriptOutput = SettingKey[File]("conscript-output")
  val conscriptBoot = SettingKey[File]("conscript-boot")
  val csWrite = TaskKey[Unit]("cs-write",
      "Write test launchconfig files to conscript-output")
  val csRun = InputKey[Unit]("cs-run",
      "Run a named launchconfig, with parameters")
  override val settings: Seq[Project.Setting[_]] = Seq(
    libraryDependencies +=
      "org.scala-tools.sbt" % "launcher-interface" % "0.10.0" % "provided",
    conscriptBase <<= (sourceDirectory in Compile) / "conscript",
    conscriptOutput <<= target / "conscript",
    conscriptBoot <<= conscriptOutput / "boot",
    csWrite <<= csWriteTask,
    csRun <<= csRunTask,
    (aggregate in csRun) := false
  )
  private def configs(path: File) = (path ** "launchconfig").get
  private def configName(path: File) =
    new java.io.File(path.getParent).getName 
  def csWriteTask =
    (conscriptBase, conscriptOutput, conscriptBoot) map {
      (base, output, boot) =>
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
      ()
    }
  def csRunTask = inputTask { (argTask: TaskKey[Seq[String]]) =>
    (argTask, conscriptOutput, csWrite, publishLocal) map {
      (args, output, _, _) =>
        import sbt.Process._
        val config = args.headOption.map { name =>
          configs(output).find { 
            p => configName(p) == name 
          }.getOrElse { error("No launchconfig found for " + name) }
        }.getOrElse { error("Usage: cs-run <appname> [args ...]") }
        "sbt @%s %s".format(config,
                            args.toList.tail.mkString(" ")
        ) ! match {
          case 0 => ()
          case n => error("Launched app error code: " + n)
        }
    }
  }
}
