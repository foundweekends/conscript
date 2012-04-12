package conscript

import dispatch._
import net.liftweb.json.JsonAST._
import java.io.File
import com.ning.http.client.{RequestBuilder=>Req}

object Github extends Credentials {
  import Conscript.http
  def lookup(user: String, repo: String, branch: String)
  : Promise[Either[String, Iterable[(String, Launchconfig)]]] = {
    def base = gh(user, repo)
    for {
      sha <- shas(base, branch).right.values.flatten
      (name, hash) <- trees(base, sha).right.values
      lc <- blob(base, hash).right
    } yield (name, Launchconfig(lc))
  }
  def shas(base: Req, branch: String) =
    http(
      base / "git" / "refs" / "heads" / branch OK LiftJson.As
    ).either.right.map { js =>
      for {
        JField("object", JObject(obj)) <- js
        JField("sha", JString(sha)) <- obj
      } yield sha
    }.left.map {
      case StatusCode(404) => "Repository not found on github"
      case e => unknownError(e)
    }
  def trees(base: Req, sha: String) =
    http(base / "git" / "trees" / sha <<? Map(
      "recursive" -> "1"
    ) OK LiftJson.As).either.right.map { js =>
      for {
        JField("tree", JArray(ary)) <- js
        JObject(obj) <- ary
        JField("path", JString(name)) <- obj
        JField("sha", JString(hash)) <- obj
        name <- Script.findFirstMatchIn(name)
      } yield (name.group(1), hash)
    }.left.map(unknownError)
  def blob(base: Req, hash: String) = {
      http((base / "git" / "blobs" / hash).addHeader(
        "Accept", "application/vnd.github.raw"
      ) OK As.string).either.left.map(unknownError)
  }
  def gh(user: String, repo: String) =
    withCredentials(:/("api.github.com").secure / "repos" / user / repo)

  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
  val unknownError = (e: Throwable) =>
    """An unexpected error occurred: Please check your internet connection.
      |And if you're using a pre-release version of Java, please try with
      |a final release version.
      |Exception message: %s""".stripMargin.format(e.getMessage)
}
