package conscript

class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = Conscript.run(config.arguments)
}

object Conscript {
  import dispatch._

  case class Config(project: String = "",
                    branch: String = "master",
                    clean_boot: Boolean = false,
                    setup: Boolean = false)

  def main(args: Array[String]) { run(Array("--setup")) }

  def run(args: Array[String]): Exit = {
    import scopt._
    var config = Config()
    val parser = new OptionParser("cs") {
      opt("clean-boot", "clears boot dir", { config = config.copy(clean_boot = true) })
      opt("setup", "installs sbt launcher", { config = config.copy(setup = true) })
      argOpt("[<user>/<project>[/<version>]]", "github project", { p => config = config.copy(project = p) })
      argOpt("[<branch>]", "github branch (default: master)", { b => config = config.copy(branch = b)})
    }
    def parse(args: Array[String]) = if (parser.parse(args)) Some(config) else None

    parse(args) map { c => (c match {
      case c if c.clean_boot && Apply.bootdir.exists && Apply.bootdir.isDirectory =>
        Clean.clean(Apply.bootdir.listFiles).toLeft("Cleaned boot directory (%s)".format(Apply.bootdir))
      case c if c.setup =>
        Apply.launchJar.right flatMap { _ =>
          configure("n8han", "conscript")
        }
      case Config(GhProject(user, repo, version), branch, _, _) => configure(user, repo, branch, Option(version))
      case _ => Left(parser.usage)
    }) fold ( { err =>
      println(err)
      Exit(1)
    }, { msg =>
      println(msg)
      Exit(0)
    })} getOrElse{Exit(1)}
  }

  def configure(user: String, repo: String, branch: String = "master", version: Option[String] = None) =
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
  case class Exit(val code: Int) extends xsbti.Exit
  val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}
