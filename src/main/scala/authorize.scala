package conscript

import dispatch._
import net.liftweb.json.JsonAST._

object Authorize {
  def auths = :/("api.github.com").secure.POST / "authorizations"

  import Conscript.http
  def apply(user: String, pass: String): Promise[Either[String, String]] =
    http(
        auths.as_!(user, pass).setBody("{}") OK LiftJson.As
    ).either.left.map {
      case StatusCode(401) => "Unrecognized github login and password"
      case e => "Unexpected error: " + e.getMessage
    }.right.flatMap {
      case JField("token", JString(token)) =>
        Config.properties {
          _.setProperty("gh.access", token)
        }
        Promise(Right("Authorization stored"))
      case _ =>
        Promise(Left("JSON parsing error"))
    }
}
