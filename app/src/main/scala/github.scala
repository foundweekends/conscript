package conscript

import dispatch._
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST._
import scala.util.control.Exception.allCatch
import java.io.File

object Github {
  def lookup(user: String, repo: String, version: Option[String]) = {
    allCatch.opt { http(gh / "blob" / "all" / user / repo / "master" ># { js =>
      for {
        blobs <- ('blobs ? obj)(js)
        JField(name, JString(sha)) <- blobs
        name <- Script.findFirstMatchIn(name)
      } yield {
        (name.group(1), http(gh / "blob" / "show" / user / repo / sha >- { str =>
          version map { v => withversion(str, if (v startsWith "/") v drop 1 else v) } getOrElse {str}
        }))
      }
    }) }.toRight {
      "Error finding for scripts for %s/%s".format(user,repo)
    }
  }

  def withversion(launchconfig: String, version: String) = {
    var section: String = ""
    (launchconfig.lines map { line =>
      """\[\w+\]""".r.findFirstIn(line.trim) map { s =>
        section = s
      }
      """version:.*""".r.findFirstIn(line.trim) match {
        case Some(_) if section == "[app]" => "  version: " + version
        case _ => line
      }
    }) mkString(System.getProperty("line.separator"))
  }

  val http = new Http {
    override def make_logger = new dispatch.Logger {
      def info(msg: String, items: Any*) { }
    }
  }
  val gh = :/("github.com").secure / "api" / "v2" / "json"
  val Script = "^src/main/conscript/([^/]+)/launchconfig$".r
}
