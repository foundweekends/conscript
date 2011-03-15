package conscript

import sbt.{FileUtilities => U,Path}

trait Harness { self: sbt.DefaultProject =>
  def conscriptBase = (mainSourcePath / "conscript") ##
  def conscriptOutput = outputPath / "conscript"
  def conscriptBoot = conscriptOutput / "boot"
  def conscriptConfigs = conscriptOutput ** "launchconfig"
  lazy val conscriptWrite = task {
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
  }
  lazy val conscriptTest = task { args =>
    import sbt.Process._
    conscriptWrite.run.map { err => task { Some(err) } } getOrElse {
      val config = args.firstOption.flatMap { name =>
        conscriptConfigs.get.toSeq.filter { 
          n => configName(n) == name 
        } firstOption
      }
      config match {
        case None =>
          task {
            args.firstOption.map { "No launchconfig found for " + _ } orElse
              Some("Usage: conscript-test <appname> [args ...]")
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
  }
  private def configName(p: Path) =
    new java.io.File(p.asFile.getParent).getName 
}
