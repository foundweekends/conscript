package conscript

import dispatch._
import dispatch.liftjson.Js._
import scala.util.control.Exception.allCatch

object Github {
  def lookup(user: String, repo: String) = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / "master" ># (
      'blobs ? obj ~> { blobs =>
        def source[T](file: String) =
          blobs.flatMap(Symbol("src/main/conscript/" + file) ? str).headOption.map {
            sha => gh / "blob" / "show" / user / repo / sha
          }
        def propmap(req: Request) =
          http(req >> { stm =>
            import scala.collection.JavaConversions._
            val p = new java.util.Properties
            p.load(stm)
            (Map.empty[String, String] /: p.propertyNames) { (m, k) =>
              m + (k.toString -> p.getProperty(k.toString))
            }
          })
        source("launchconfig").map { req =>
          val lc = http(req as_str)
          (lc, source("default.properties").map(propmap).getOrElse { Map.empty })
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
