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
    lazy val csSbtLauncherVersion = settingKey[String]("sbt launcher version")
  }

  import autoImport._
  override def projectSettings: Seq[Def.Setting[_]] =
    List(
      csSbtLauncherVersion := "1.0.1",
      libraryDependencies += "org.scala-sbt" % "launcher" % csSbtLauncherVersion.value % "provided",
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
  private[this] val conscriptHome: File =
    sys.env.get("CONSCRIPT_HOME") match {
      case Some(x) => new File(x)
      case None => new File(System.getProperty("user.home"), ".conscript")
    }
  lazy val csRunTask = Def.inputTask {
    val args = Def.spaceDelimited().parsed
    val x = csWrite.value
    val y = publishLocal.value
    val output = (target in csRun).value
    val config = args.headOption.map { name =>
      configs(output).find {
        p => configName(p) == name
      }.getOrElse { sys.error("No launchconfig found for " + name) }
    }.getOrElse { sys.error("Usage: csRun <appname> [args ...]") }

    val launcherVersion = csSbtLauncherVersion.value
    val launcher = s"launcher-$launcherVersion.jar"
    val launcherFile = conscriptHome / launcher
    if(!launcherFile.exists) {
      IO.download(url(s"https://oss.sonatype.org/content/repositories/public/org/scala-sbt/launcher/$launcherVersion/launcher-$launcherVersion.jar"), launcherFile)
    }
    val f = new sbt.ForkRun(ForkOptions())
    f.run(
      mainClass = "xsbt.boot.Boot",
      classpath = launcherFile :: Nil,
      options = ("@" + config.toString) :: args.toList.tail,
      log = streams.value.log
    )
  }
}
