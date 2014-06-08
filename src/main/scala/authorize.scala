package conscript

import dispatch._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.{renderJValue, compactJson}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Authorize {
  def auths = :/("api.github.com").secure.POST / "authorizations"

  import Conscript.http
  def apply(user: String, pass: String): Future[Either[String, String]] =
    http(
        auths.as_!(user, pass).setBody(compactJson(renderJValue(
          ("note" -> "Conscript") ~
          ("note_url" -> "https://github.com/n8han/conscript") ~
          ("scopes" -> ("repo" :: Nil))
        ))) OK Json.As
    ).either.left.map {
      case StatusCode(401) => "Unrecognized github login and password"
      case e => "Unexpected error: " + e.getMessage
    }.map { _.right.flatMap { js =>
      (for {
        JObject(fields) <- js
        JField("token", JString(token)) <- fields
      } yield {
        Config.properties {
          _.setProperty("gh.access", token)
        }
        "Authorization stored"
      }).headOption.toRight("JSON parsing error")
    } }
}
