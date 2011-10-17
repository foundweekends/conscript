package conscript

import dispatch._
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._
import scala.util.control.Exception.allCatch
import java.io.File

object Github extends Credentials {
  import Conscript.http

  def lookup(user: String, repo: String, branch: String) = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / branch ># { js =>
      for {
        blobs <- ('blobs ? obj)(js)
        JField(name, JString(sha)) <- blobs
        name <- Script.findFirstMatchIn(name)
      } yield {
        (name.group(1), http(gh / "blob" / "show" / user / repo / sha >- { str =>
          Launchconfig(str)
        }))
      }
    }) }.toRight {
      "Error finding for scripts for %s/%s".format(user,repo)
    }
  }
    
  val gh = withCredentials(:/("github.com").secure / "api" / "v2" / "json")
  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
