package conscript

class Conscript extends xsbti.AppMain {
  import dispatch._
  def run(config: xsbti.AppConfiguration) = {
    val (cleanopt, repo) = config.arguments.partition(_ == "--clean-boot")
    val result = cleanopt.headOption.filter { _ =>
      Apply.bootdir.exists && Apply.bootdir.isDirectory
    }.map { _ =>
      Clean.clean(Apply.bootdir.listFiles).toLeft(
        "Cleaned boot directory (%s)".format(Apply.bootdir)
      )
    }.getOrElse(Right("")).right.flatMap { in => (cleanopt, repo) match {
      case (_, Array(GhProject(user, repo, version))) =>
        Github.lookup(user, repo, Option(version)).right.flatMap {
          case Nil => Left("No scripts found for %s/%s".format(user,repo))
          case scripts =>
            ((Right(in): Either[String, String]) /: scripts) { 
              case (either, (name, launch)) =>
                either.right.flatMap { cur =>
                  Apply.config(user, repo, name, launch).right.map { 
                    cur + "\n" +  _
                  }
                }
            }
        }
      case (Array(opt, _*), Array()) => Right(in)
      case _ => Left(usage)
    } }
    result fold ( { err =>
      println(err)
      Exit(1)
    }, { msg =>
      println(msg)
      Exit(0)
    })
  }
  case class Exit(val code: Int) extends xsbti.Exit
  def usage = """Usage: cs [OPTION] [USER/PROJECT]"""
  val GhProject = "([^/]+)/([^/]+)(/[^/]+)?".r
}
