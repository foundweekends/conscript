package conscript

import java.util.Properties
import java.io.{File, FileInputStream}

import collection.JavaConversions._

trait Credentials {
  def withCredentials(req: dispatch.Request) =
    credentials map { case (user, pass) => req as_! (user,pass) } getOrElse req

  lazy val credentials: Option[(String,String)] = {
    val props = readProps(new File(System.getProperty("user.home"), ".gh"))
    val auth = for {
      user <- props get "username"
      pass <- (props get "token") orElse (props get "password")
    } yield (user, pass)
    auth.map {
      case (user, pass) if props isDefinedAt "token" => (user + "/token", pass)
      case creds => creds
    }
  }

  val readProps: PartialFunction[File,Map[String,String]] = {
    case file if file.exists =>
      val p = new java.util.Properties
      val fis = new FileInputStream(file)
      p.load(fis)
      fis.close
      (Map.empty[String,String] /: p.propertyNames) { 
        case (m, prop) => m + (prop.toString -> p.getProperty(prop.toString))
      }
    case _ => Map.empty[String,String]
  }
}
