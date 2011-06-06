package conscript

class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = Conscript.run(config.arguments)
}

object Conscript {
  import dispatch._

  def main(args: Array[String]) { run(Array("--setup")) }

  def run(args: Array[String]) = {
    val (cleanopt, setupopt, repo) = args.partition(_ == "--clean-boot") match {
      case (cleanopt, rest) =>
        val (setupopt, repo) = rest.partition(_ == "--setup")
        (cleanopt, setupopt, repo)
    }

    val result: Either[String, String] = (cleanopt.headOption, setupopt.headOption, repo) match {
      case (Some(x), _, _) if Apply.bootdir.exists && Apply.bootdir.isDirectory =>
        Clean.clean(Apply.bootdir.listFiles).toLeft("Cleaned boot directory (%s)".format(Apply.bootdir))
      case (_, Some(x), _) =>
        Apply.launchJar.right flatMap { _ =>
          configure("n8han", "conscript")
        }
      case (_, _, Array(GhProject(user, repo))) => configure(user, repo)
      case _ => Left(usage)
    }

    result fold ( { err =>
      println(err)
      Exit(1)
    }, { msg =>
      println(msg)
      Exit(0)
    })
  }

  def configure(user: String, repo: String) = Github.lookup(user, repo).right.flatMap {
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
  def usage = """Usage: cs [OPTION] [USER/PROJECT]"""
  val GhProject = "([^/]+)/([^/]+)".r
}
