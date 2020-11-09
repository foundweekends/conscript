package conscript

import dispatch._
import net.liftweb.json.JsonAST._
import com.ning.http.client.ProxyServer
import scala.concurrent.{ Future, Promise }

object Github extends Credentials {
  import scala.concurrent.ExecutionContext.Implicits.global
  import Conscript.http
  val DefaultBranch = "master"

  def lookup(user: String, repo: String, branch: Option[String])
  : Future[Either[String, Iterable[(String, Launchconfig)]]] = {
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
    }.left.map(errorStatusesToMessages)
  def trees(base: Req, sha: String) =
    http(base / "git" / "trees" / sha <<? Map(
      "recursive" -> "1"
    ) OK LiftJson.As).either.left.map(errorStatusesToMessages).map { eth =>
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
  def guaranteed[L, R](value: R): Future[Either[L, R]] =
    {
      val p = Promise[Either[L, R]]()
      p.success(Right(value))
      p.future
    }
  def refname(given: Option[String], base: Req) =
    given match {
        case Some(branch) => guaranteed[String, String](branch).right
        case _ => masterBranch(base).left.flatMap {
          case _ => guaranteed[String, String](DefaultBranch)
        }.right
      }
  def masterBranch(base: Req) = {
    http(base OK LiftJson.As).either.left.map(errorStatusesToMessages).map { eth =>
      eth.right.flatMap { js =>
        (for{
          JObject(obj) <- js
          JField("default_branch", JString(branch)) <- obj
        } yield branch) match {
          case Seq() => Left("Default master branch not found on github")
          case seq => Right(seq.head)
        }
      }
    }
  }
  def blob(base: Req, hash: String) = {
      http((base / "git" / "blobs" / hash).addHeader(
        "Accept", "application/vnd.github.raw"
      ) OK as.String).either.left.map(errorStatusesToMessages)
  }
  def gh(user: String, repo: String) : Req = {
    val req = withCredentials(:/("api.github.com").secure / "repos" / user / repo)

    for {
      host <- Option(System getProperty "https.proxyHost")
      port <- Option(System getProperty "https.proxyPort")
    } {
      req.setProxyServer(new ProxyServer(host, port.toInt))
    }

    return req
  }

  val errorStatusesToMessages: (Throwable) => String = {
    case StatusCode(404) => "Repository not found on github"
    case StatusCode(403) =>
      """Github responded with HTTP 403 Forbidden.
        |You may need to generate a github access token.
        |see https://help.github.com/articles/creating-an-access-token-for-command-line-use/""".stripMargin
    case e =>
      """An unexpected error occurred: Please check your internet connection.
        |Exception message: %s""".stripMargin.format(e.getMessage)
  }
  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
