package conscript

import dispatch._
import com.ning.http.client.RequestBuilder

trait Credentials {
  import scala.util.control.Exception.allCatch
  
  def withCredentials(req: RequestBuilder) =
    (oauth map { 
      case token => req.addHeader("Authorization", "token %s".format(token))
    }).getOrElse { req }
  
  def oauth: Option[String] =
    Config.get("gh.access")
  
  def windows =
    System.getProperty("os.name") match {
      case x: String if x contains "Windows" => Some(x)
      case _ => None
    }
}
