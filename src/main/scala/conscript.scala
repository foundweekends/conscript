package conscript

import scala.util.control.Exception.allCatch

object Conscript {
  import dispatch._
  import Apply.exec
  val http = dispatch.Http

  case class Config(project: String = "",
                    branch: String = "master",
                    clean_boot: Boolean = false,
                    setup: Boolean = false,
                    usage: Boolean = false,
                    shouldExec: Boolean = true,
                    entries: Seq[ConfigEntry] = Nil
                  )

  /** This is the entrypoint for the runnable jar, as well as
   * the sbt `run` action when in the conscript project. */
  def main(args: Array[String]) {
    val exit = run(args match {
      case Array() => Array("--setup")
      case _ => args
    })
    // not using exit value here, to leave any swing window open
  }

  /** Shared by the launched version and the runnable version */
  def run(args: Array[String]): Int = {
    import scopt._
    var config = Config()
    val parser = new OptionParser("cs", BuildInfo.version) {
      opt("clean-boot", "clears boot dir", {
        config = config.copy(clean_boot = true)
      })
      opt("setup", "installs sbt launcher", {
        config = config.copy(setup = true)
      })
      opt("b", "branch", "github branch (default: master)", { b => 
        config = config.copy(branch = b)
      })
      opt("local", "include local repos", {
        config = config.copy(entries = config.entries :+ InsertLocalRepository)
      })
      opt("no-local", "exclude local and maven-local repos", {
        config = config.copy(entries = config.entries ++ Seq(RemoveLocalRepository, RemoveMavenLocalRepository))
      })      
      opt("version", "print current version", {
        config = config.copy(usage = true)
      })
      opt("no-exec", "don't execute program after install", {
        config = config.copy(shouldExec = false)
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
          configure("n8han",
                    "conscript",
                    true,
                    configoverrides = Seq(ConfigVersion(BuildInfo.version))
          ).right.flatMap { msg =>
            display.info(msg)
            examine("cs")
          }
        }
      case Config(GhProject(user, repo, version), branch, _, _, _, shouldExec, entries) =>
        configure(user, repo, shouldExec, branch, entries ++ (Option(version) map { v => ConfigVersion(v) }).toSeq)
      case _ => Left(parser.usage)
    }.getOrElse { Left(parser.usage) }.fold( { err =>
      display.error(err)
      http.shutdown
      1
    }, { msg =>
      display.info(msg)
      http.shutdown
      0
    })
  }

  def examine(scr: String): Either[String,String] = {
    allCatch.opt { exec(scr) } match {
      case Some(0) =>
        Right("Success!\n%s is at your command.".format(scr))
      case _ =>
        val pathed = Apply.scriptFile(scr).toString
        allCatch.opt { exec(pathed) } match {
          case Some(0) =>
            Right("Installed: %s\nMay not be on executable PATH".format(pathed))
          case _ =>
            Left("Installed: %s\nError reported; run from terminal for details.".format(pathed))
        }
    }            
  }

  def configure(user: String,
                repo: String,
                shouldExec: Boolean,
                branch: String = "master",
                configoverrides: Seq[ConfigEntry] = Nil) =
    Github.lookup(user, repo, branch).map { result =>
      result.right.flatMap { scripts =>
        ((Right(""): Either[String,String]) /: scripts) {
          case (either, (name, launch)) =>
            either.right.flatMap { cur =>
              val modLaunch = (launch /: configoverrides) {_ update _}
              Apply.config(user, repo, name, modLaunch, shouldExec).right.map {
                cur + "\n" +  _
              }
            }
          }
      }
    }()
  val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}

/** The launched conscript entry point */
class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) =
    new Exit(Conscript.run(config.arguments))
}
class Exit(val code: Int) extends xsbti.Exit
