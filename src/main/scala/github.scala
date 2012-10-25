package conscript

import dispatch._
import net.liftweb.json.JsonAST._
import java.io.File
import com.ning.http.client.{RequestBuilder=>Req}
import com.ning.http.client.ProxyServer

object Github extends Credentials {
  import Conscript.http
  val DefaultBranch = "master"

  def lookup(user: String, repo: String, branch: Option[String])
  : Promise[Either[String, Iterable[(String, Launchconfig)]]] = {
    def base = gh(user, repo)
    for {
      ref <- refname(branch, base)
      sha <- shas(base, ref).right.values.flatten
      (name, hash) <- trees(base, sha).right.values
      lc <- blob(base, hash).right
    } yield (name, Launchconfig(lc))
  }
  def shas(base: Req, ref: String) =
    http(
      base / "git" / "refs" / "heads" / ref OK LiftJson.As
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
    ) OK LiftJson.As).either.left.map(unknownError).map { eth =>
      eth.right.flatMap { js =>
        (for {
          JField("tree", JArray(ary)) <- js
          JObject(obj) <- ary
          JField("path", JString(name)) <- obj
          JField("sha", JString(hash)) <- obj
          name <- Script.findFirstMatchIn(name)
        } yield (name.group(1), hash)) match {
          case Seq() => Left("No conscripts found in this repository")
          case seq => Right(seq)
        }
      }
    }
  def guaranteed[L, R](value: R) =
    Promise((Right(value): Either[L, R]))
  def refname(given: Option[String], base: Req) =
    given match {
        case Some(branch) => guaranteed[String, String](branch).right
        case _ => masterBranch(base).left.flatMap {
          case _ => guaranteed[String, String](DefaultBranch)
        }.right
      }
  def masterBranch(base: Req) =
    http(base OK LiftJson.As).either.left.map {
      case StatusCode(404) => "Repository not found on github"
      case e => unknownError(e)
    }.map { eth =>
      eth.right.flatMap { js =>
        (for{
          JObject(obj) <- js
          JField("master_branch", JString(branch)) <- obj
        } yield branch) match {
          case Seq() => Left("Default master branch not found on github")
          case seq => Right(seq.head)
        }
      }
    }
  def blob(base: Req, hash: String) = {
      http((base / "git" / "blobs" / hash).addHeader(
        "Accept", "application/vnd.github.raw"
      ) OK As.string).either.left.map(unknownError)
  }
  def gh(user: String, repo: String) : Req = {
    val req = withCredentials(:/("api.github.com").secure / "repos" / user / repo)

    if(System.getProperty("https.proxyHost") != null && System.getProperty("https.proxyPort") != null) {
      req.setProxyServer(new ProxyServer(System.getProperty("https.proxyHost"), 
        System.getProperty("https.proxyPort").toInt))
    }
    
    return req
  }
    

  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
  val unknownError = (e: Throwable) =>
    """An unexpected error occurred: Please check your internet connection.
      |And if you're using a pre-release version of Java, please try with
      |a final release version.
      |Exception message: %s""".stripMargin.format(e.getMessage)
}
