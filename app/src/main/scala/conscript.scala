package conscript

import scala.util.control.Exception.{allCatch,catching}

object Conscript {
  import dispatch._
  import Apply.exec
  val http = new Http with NoLogging

  case class Config(project: String = "",
                    branch: String = "master",
                    clean_boot: Boolean = false,
                    setup: Boolean = false,
                    usage: Boolean = false)

  /** This is the entrypoint for the runnable jar, as well as
   * the sbt `run` action when in the conscript project. */
  def main(args: Array[String]) {
    val exit = run(args match {
      case Array() => Array("--setup")
      case _ => args
    })
    // not using exit value, may add for headless mode
  }

  /** Shared by the launched version and the runnable version */
  def run(args: Array[String]): Int = {
    import scopt._
    var config = Config()
    val parser = new OptionParser("cs", Version.version) {
      opt("clean-boot", "clears boot dir", {
        config = config.copy(clean_boot = true)
      })
      opt("setup", "installs sbt launcher", {
        config = config.copy(setup = true)
      })
      opt("b", "branch", "github branch (default: master)", { b => 
        config = config.copy(branch = b)
      })
      opt("version", "print current version", {
        config = config.copy(usage = true)
      })
      argOpt("[<user>/<project>[/<version>]]", "github project", { p =>
        config = config.copy(project = p)
      })
    }
    val parsed =
      if (parser.parse(args)) Some(config)
      else None
    val display =
      if (config.setup)
        allCatch.opt {
          SplashDisplay
        }.getOrElse(ConsoleDisplay)
      else ConsoleDisplay

    parsed.map {
      case c if c.clean_boot =>
        if (Apply.bootdir.exists && Apply.bootdir.isDirectory)
          Clean.clean(Apply.bootdir.listFiles).toLeft("Cleaned boot directory (%s)".format(Apply.bootdir))
        else Left("No boot directory found at " + Apply.bootdir)
      case c if c.usage =>
        Right(parser.usage)
      case c if c.setup =>
        Apply.launchJar(display).right flatMap { msg =>
          display.info(msg)
          configure("n8han", "conscript").right.flatMap { msg =>
            display.info(msg)
            examine("cs")
          }
        }
      case Config(GhProject(user, repo, version), branch, _, _, _) =>
        configure(user, repo, branch, Option(version))
      case _ => Left(parser.usage)
    }.getOrElse { Left(parser.usage) }.fold( { err =>
      display.error(err)
      1
    }, { msg =>
      display.info(msg)
      0
    })
  }

  def examine(scr: String): Either[String,String] = {
    allCatch.opt { exec(scr) } match {
      case Some(0) =>
        Right("Success! `%s` is at your command.".format(scr))
      case _ =>
        val pathed = Apply.scriptFile(scr).toString
        allCatch.opt { exec(pathed) } match {
          case Some(0) =>
            Left("Installed to %s but you should the directory to your executable path.".format(pathed))
          case _ =>
            Left("Fail. Run %s on a command line for details.".format(pathed))
        }
    }            
  }

  def configure(user: String,
                repo: String,
                branch: String = "master",
                version: Option[String] = None) =
    Github.lookup(user, repo, branch, version).right.flatMap {
      case Nil => Left("No scripts found for %s/%s".format(user,repo))
      case scripts =>
        ((Right(""): Either[String, String]) /: scripts) {
          case (either, (name, launch)) =>
            either.right.flatMap { cur =>
              Apply.config(user, repo, name, launch).right.map {
                cur + "\n" +  _
              }
            }
        }
    }
  val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}

/** The launched conscript entry point */
class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) =
    new Exit(Conscript.run(config.arguments))
}
class Exit(val code: Int) extends xsbti.Exit
