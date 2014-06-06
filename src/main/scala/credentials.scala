package conscript

import dispatch.Req

trait Credentials extends OsDetect {
  def withCredentials(req: Req) =
    (oauth map { 
      case token => req.addHeader("Authorization", "token %s".format(token))
    }).getOrElse { req }
  
  def oauth: Option[String] =
    Config.get("gh.access")
  
}

trait OsDetect {
  def windows =
    System.getProperty("os.name") match {
      case x: String if x contains "Windows" => Some(x)
      case _ => None
    }

  def isXP = windows.map(_.contains("XP")).getOrElse(false)
}
