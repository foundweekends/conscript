package conscript

import sbt.{FileUtilities => U,Path}

trait Harness { self: sbt.BasicDependencyProject =>
    val launchInterface =
      "org.scala-tools.sbt" % "launcher-interface" % "0.7.5" % "provided" from
      "http://databinder.net/repo/org.scala-tools.sbt/launcher-interface/0.7.5/jars/launcher-interface.jar"

  // can not use mainSourcePath, ParentProject does not implement
  def conscriptBase = (path("src") / "main" / "conscript") ##
  def conscriptOutput = outputPath / "conscript"
  def conscriptBoot = conscriptOutput / "boot"
  def conscriptConfigs = conscriptOutput ** "launchconfig"
  lazy val csWrite = task {
    U.clean(conscriptOutput, log) orElse
    U.copyDirectory(conscriptBase, conscriptOutput, log) orElse
    ((None: Option[String]) /: conscriptConfigs.get) {
      (err, path) => err.orElse(
        U.append(path.asFile, 
               """
               |[boot]
               |  directory: %s
               |""".stripMargin.format(conscriptBoot), log)
      )
    }
  } describedAs "Write test launchconfig out to " + conscriptOutput
  lazy val csRun = task { args =>
    import sbt.Process._
    csWrite.run.map { err => task { Some(err) } } getOrElse {
      val config = args.firstOption.flatMap { name =>
        conscriptConfigs.get.toSeq.filter { 
          n => configName(n) == name 
        } firstOption
      }
      config match {
        case None =>
          task {
            args.firstOption.map { "No launchconfig found for " + _ } orElse
              Some("Usage: cs-run <appname> [args ...]")
          }
        case Some(cfg) =>
          task { 
            "sbt @%s %s".format(cfg, 
                                args.toList.tail.mkString(" ")) ! log match {
              case 0 => None
              case n => Some("Launched app error code: " + n)
            }
          } dependsOn publishLocal
      }
    }
  } completeWith {
    conscriptConfigs.get.map(configName).toSeq
  } describedAs "Run a named launchconfig, with parameters"
  private def configName(p: Path) =
    new java.io.File(p.asFile.getParent).getName 
}
