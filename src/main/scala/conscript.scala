package conscript

import scala.util.control.Exception.allCatch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

object Conscript {
  import dispatch.{ BuildInfo => _, _ }
  import Apply.exec
  val http = dispatch.Http.configure { configBuilder ⇒
    configBuilder.setFollowRedirects(true)
  }

  case class Config(project: String = "",
                    branch: Option[String] = None,
                    clean_boot: Boolean = false,
                    setup: Boolean = false,
                    usage: Boolean = false,
                    shouldExec: Boolean = true,
                    entries: Seq[ConfigEntry] = Nil,
                    auth: Option[String] = None
                  )

  /** This is the entrypoint for the runnable jar, as well as
   * the sbt `run` action when in the conscript project. */
  def main(args: Array[String]): Unit = {
    val exit = run(args match {
      case Array() => Array("--setup")
      case _ => args
    })
    // not using exit value here, to leave any swing window open
  }

  /** Shared by the launched version and the runnable version */
  def run(args: Array[String]): Int = {
    import scopt._
    val parser = new OptionParser[Config]("cs") {
      head("Conscript", conscript.BuildInfo.version)
      opt[Unit]("clean-boot").text("clears boot dir") action { (x, config) =>
        config.copy(clean_boot = true)
      }
      opt[Unit]("setup").text("installs sbt launcher") action { (x, config) =>
        config.copy(setup = true)
      }
      opt[String]('b', "branch").text("github branch (default: master)") action { (b, config) =>
        config.copy(branch = Some(b))
      }
      opt[String]('a', "auth").text("obtain oauth token with <name>:<password>") action { (b, config) =>
        config.copy(auth = Some(b))
      }
      opt[Unit]("local").text("include local repos") action { (x, config) =>
        config.copy(entries = config.entries :+ InsertLocalRepository)
      }
      opt[Unit]("no-local").text("exclude local and maven-local repos") action { (x, config) =>
        config.copy(entries = config.entries ++ Seq(RemoveLocalRepository, RemoveMavenLocalRepository))
      }
      opt[Unit]("version").text("print current version") action { (x, config) =>
        config.copy(usage = true)
      }
      opt[Unit]("no-exec").text("don't execute program after install") action { (x, config) =>
        config.copy(shouldExec = false)
      }
      arg[String]("[<user>/<project>[/<version>]]").text("github project").optional() action { (p, config) =>
        config.copy(project = p)
      }
    }
    val parsed = parser.parse(args, Config())
    val display =
      parsed match {
        case Some(config) if config.setup =>
          try {
            SplashDisplay
          } catch {
            case NonFatal(e) =>
              ConsoleDisplay
          }
        case _ => ConsoleDisplay
      }

    parsed.map {
      case c if c.clean_boot =>
        if (Apply.bootdir.exists && Apply.bootdir.isDirectory)
          Clean.clean(Apply.bootdir).toLeft("Cleaned boot directory (%s)".format(Apply.bootdir))
        else Left("No boot directory found at " + Apply.bootdir)
      case c if c.usage =>
        Right(parser.usage)
      case c if !c.auth.isEmpty =>
        c.auth.get.split(":",2) match {
          case Array(name, pass) => Authorize(name, pass)()
          case _ => Left("-a / --auth requires <name>:<pass>")
        }
      case c if c.setup =>
        Apply.launchJar(display).right flatMap { msg =>
          display.info(msg)
          configure("foundweekends",
                    "conscript",
                    true,
                    configoverrides = Seq(ConfigVersion(conscript.BuildInfo.version))
          ).right.flatMap { msg =>
            display.info(msg)
            examine("cs")
          }
        }
      case Config(GhProject(user, repo, version), branch, _, _, _, shouldExec, entries, _) =>
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
                branch: Option[String] = None,
                configoverrides: Seq[ConfigEntry] = Nil) =
    {
      val f = Github.lookup(user, repo, branch).map { result =>
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
      }
      f()
    }
  val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}

/** The launched conscript entry point */
class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) =
    new Exit(Conscript.run(config.arguments))
}
class Exit(val code: Int) extends xsbti.Exit
