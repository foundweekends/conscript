package conscript

import dispatch._
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._
import scala.util.control.Exception.allCatch

object Github extends Launch {
  def lookup(user: String, repo: String) = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / "master" ># { js =>
      for {
        blobs <- ('blobs ? obj)(js)
        JField(name, JString(sha)) <- blobs
        name <- Script.findFirstMatchIn(name)
      } yield {
        (name.group(1), http(gh / "blob" / "show" / user / repo / sha as_str))
      }
    }) }.toRight {
      "Error finding for scripts for %s/%s".format(user,repo)
    }
  }

  val gh = :/("github.com").secure / "api" / "v2" / "json"
  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
