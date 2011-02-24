package conscript

class Conscript extends xsbti.AppMain {
  import dispatch._
  def run(config: xsbti.AppConfiguration) = {
    val result = config.arguments match {
      case Array(GhProject(user, repo)) =>
        Github.lookup(user, repo).right.flatMap {
          case Nil => Left("No scripts found for %s/%s".format(user,repo))
          case scripts =>
            ((Right(""): Either[String,String]) /: scripts) { 
              case (either, (name, launch)) =>
                either.right.flatMap { cur =>
                  Apply.config(user, repo, name, launch).right.map { cur + "\n" +  _ }
                }
            }
        }
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
  case class Exit(val code: Int) extends xsbti.Exit
  def usage = """Usage: cs <user/project>"""
  val GhProject = "([^/]+)/([^/]+)".r
}
