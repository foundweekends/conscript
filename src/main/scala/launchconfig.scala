package conscript

import dispatch._
import dispatch.liftjson.Js._
import scala.util.control.Exception.allCatch

object LaunchConfig {
  def lookup(user: String, repo: String): Either[String, String] = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / "master" ># (
      'blobs ? obj >>~> Symbol("src/main/conscript/launchconfig") ? str >~> { sha =>
        http(gh / "blob" / "show" / user / repo / sha as_str)
      }
    )).head }.toRight(
      "No launchconfig found for %s/%s".format(user,repo)
    )
  }

  val http = new Http
  val gh = :/("github.com") / "api" / "v2" / "json"
}
