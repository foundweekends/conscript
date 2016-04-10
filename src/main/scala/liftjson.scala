package conscript

import com.ning.http.client.Response

import net.liftweb.json._

object LiftJson {
  def As(res: Response) = JsonParser.parse(dispatch.as.String(res))
}
