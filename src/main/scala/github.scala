package conscript

import dispatch._
import net.liftweb.json.JsonAST._
import java.io.File
import com.ning.http.client.{RequestBuilder=>Req}

object Github extends Credentials {
  import Conscript.http
  def lookup(user: String, repo: String, branch: String)
  : Promise[Iterable[(String, Launchconfig)]] = {
    val base = gh(user, repo)
    
    for {
      sha <- shas(base, branch).values.flatten
      (name, hash) <- trees(base, sha).values
      lc <- blob(base, hash)
    } yield (name, Launchconfig(lc))
  }
  def shas(base: Req, branch: String): Promise[Iterable[String]] =
    http(
      base / "git" / "refs" / "heads" / branch OK LiftJson.As
    ).map { js =>
      for {
        JField("object", JObject(obj)) <- js
        JField("sha", JString(sha)) <- obj
      } yield sha
    }
  def trees(base: Req, sha: String): Promise[Iterable[(String,String)]] =
    http(base / "git" / "trees" / sha <<? Map(
      "recursive" -> "1"
    ) OK LiftJson.As).map { js =>
      for {
        JField("path", JString(name)) <- js
        JField("sha", JString(hash)) <- js
        name <- Script.findFirstMatchIn(name)
      } yield (name.group(1), hash)
    }
  def blob(base: Req, hash: String): Promise[String] = {
      http((base / "git" / "blobs" / hash).addHeader(
        "Accept", "application/vnd.github.raw"
      ) OK As.string)
  }
  def gh(user: String, repo: String) =
    withCredentials(:/("github.com").secure / "repos" / user / repo)

  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
