package conscript

import dispatch._
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._
import java.io.File

object Github extends Credentials {
  import Conscript.http

  def lookup(user: String, repo: String, branch: String)
  : Promise[Traversable[(String, Launchconfig)]] =
    http(gh / "blob" / "all" / user / repo / branch > liftjson.As).map(
      'blobs ? obj
    ).flatMap { js =>
      Promise.all(for {
        JField(name, JString(sha)) <- js
        name <- Script.findFirstMatchIn(name)
      } yield {
        http(gh / "blob" / "show" / user / repo / sha > As.string).map {
          s => (name.group(1), Launchconfig(s))
        }
      })
    }
    
  def gh = withCredentials(:/("github.com").secure / "api" / "v2" / "json")
  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
