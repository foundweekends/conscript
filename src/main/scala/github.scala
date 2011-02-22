package conscript

import dispatch._
import dispatch.liftjson.Js._
import scala.util.control.Exception.allCatch

object Github {
  def lookup(user: String, repo: String) = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / "master" ># (
      'blobs ? obj ~> { blobs =>
        def source(file: String) =
          blobs.flatMap(Symbol("src/main/conscript/" + file) ? str).headOption.map {
            sha => http(gh / "blob" / "show" / user / repo / sha as_str)
          }
        source("launchconfig").map {
          lc => (lc, source("default.properties"))
        }
      }
    )) }.flatMap { o => o }.toRight {
      "No launchconfig found for %s/%s".format(user,repo)
    }
  }

  val http = new Http {
    override def make_logger = new dispatch.Logger {
      def info(msg: String, items: Any*) { }
    }
  }
  val gh = :/("github.com").secure / "api" / "v2" / "json"
}
