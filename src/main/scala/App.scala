package conscript

class Conscript extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    (config.arguments match {
      case Array(GhProject(user, repo)) =>
        Right("%s %s" format (user, repo))
      case _ =>
        Left(usage)
    }) fold ( { err =>
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
