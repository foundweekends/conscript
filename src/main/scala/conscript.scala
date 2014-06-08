package conscript

import scala.util.control.Exception.allCatch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

object Conscript {
  import Apply.exec
  val http = dispatch.Http

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
    val parser = new OptionParser[Config]("cs") {
      opt[Unit]("clean-boot").text("clears boot dir").action(
        (_, c) => c.copy(clean_boot = true)
      )
      opt[Unit]("setup").text("installs sbt launcher").action(
        (_, c) => c.copy(setup = true)
      )
      opt[String]('b', "branch").text("github branch (default: master)").action(
        (b, c) => c.copy(branch = Some(b))
      )
      opt[String]('a', "auth").text("obtain oauth token with <name>:<password>").action(
        (b, c) => c.copy(auth = Some(b))
      )
      opt[Unit]("local").text("include local repos").action(
        (_, c) => c.copy(entries = c.entries :+ InsertLocalRepository)
      )
      opt[Unit]("no-local").text("exclude local and maven-local repos").action(
        (_, c) => c.copy(entries = c.entries ++ Seq(RemoveLocalRepository, RemoveMavenLocalRepository))
      )
      opt[Unit]("version").text("print current version").action(
        (_, c) => c.copy(usage = true)
      )
      opt[Unit]("no-exec").text("don't execute program after install").action(
        (_, c) => c.copy(shouldExec = false)
      )
      arg[String]("[<user>/<project>[/<version>]]").optional.text("github project").action(
        (p, c) => c.copy(project = p)
      )
    }
    val parsed = parser.parse(args, new Config())
    val display =
      if (parsed.exists(_.setup))
        allCatch.opt {
          SplashDisplay
        }.getOrElse(ConsoleDisplay)
      else ConsoleDisplay

    parsed.map {
      case c if c.clean_boot =>
        if (Apply.bootdir.exists && Apply.bootdir.isDirectory)
          Clean.clean(Apply.bootdir).toLeft("Cleaned boot directory (%s)".format(Apply.bootdir))
        else Left("No boot directory found at " + Apply.bootdir)
      case c if c.usage =>
        Right(parser.usage)
      case c if !c.auth.isEmpty =>
        c.auth.get.split(":",2) match {
          case Array(name, pass) => Await.result(Authorize(name, pass), 30.seconds)
          case _ => Left("-a / --auth requires <name>:<pass>")
        }
      case c if c.setup =>
        Apply.launchJar(display).right flatMap { msg =>
          display.info(msg)
          configure("n8han",
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
                configoverrides: Seq[ConfigEntry] = Nil) = {
    val future = Github.lookup(user, repo, branch).map { result =>
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
    Await.result(future, 30.seconds)
  }

  private val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}

/** The launched conscript entry point */
class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) =
    new Exit(Conscript.run(config.arguments))
}
class Exit(val code: Int) extends xsbti.Exit
