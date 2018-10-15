package conscript

sealed trait ConfigPosition
case object Insert extends ConfigPosition
case object Append extends ConfigPosition
case object InPlace extends ConfigPosition
case object Remove extends ConfigPosition

trait ConfigEntry {
  val section: String
  val key: String
  val value: Option[String]
  val position: ConfigPosition = InPlace
}

case class ConfigVersion(version: String) extends ConfigEntry {
  val section = "[app]"
  val key = "version"
  val value =
    if (version startsWith "/") Some(version drop 1)
    else Some(version)  
}

case class ConfigBootDir(dir: String) extends ConfigEntry {
  val section = "[boot]"
  val key = "directory"
  val value = Some(dir)
}

case object ConfigVerbose extends ConfigEntry {
  val section = "[log]"
  val key = "level"
  val value = Some("debug")
}

trait RepositoryConfigEntry extends ConfigEntry {
  val section = "[repositories]"
  val value = None    
}

case object InsertLocalRepository extends RepositoryConfigEntry {
  val key = "local"
   // this puts this entry ahead of all other repos
  override val position = Insert
}

case object RemoveLocalRepository extends RepositoryConfigEntry {
  val key = "local"
  override val position = Remove
}

case object RemoveMavenLocalRepository extends RepositoryConfigEntry {
  val key = "maven-local"
  override val position = Remove
}

case class Launchconfig(configstring: String) {  
  import scala.collection.mutable
  import mutable.ArrayBuffer
  
  override def toString = configstring
  
  def update(e: ConfigEntry): Launchconfig =
    updateValue(e.section, e.key, e.value, e.position)
  
  def updateValue(section: String, key: String, v: Option[String], position: ConfigPosition): Launchconfig = {
    val (sections, values) = parseLaunchconfig
    val entry = v map { "  %s: %s".format(key, _) } getOrElse {"  %s".format(key)}
    val findKey = v map { _ => key + ":" } getOrElse {key}
    sections find {_ == section} map { _ =>
      val without = values(section).toSeq filterNot { _.trim.startsWith(findKey) }
      position match {
        case Remove => values(section) = ArrayBuffer[String](without: _*)
        case Insert => values(section) = ArrayBuffer[String](Seq(entry) ++ without: _*)
        case Append => values(section) = ArrayBuffer[String](without ++ Seq(entry): _*)
        case InPlace =>
          values(section) find { _.trim.startsWith(findKey) } map { _ =>
            values(section) = values(section) map {
              case x if x.trim.startsWith(findKey) => entry
              case x => x
            }
          } getOrElse {
            values(section) += entry
          }
      }
    } getOrElse {
      if (position != Remove) {
        sections += section
        values(section) = ArrayBuffer[String](entry)
      }    
    }
    buildLaunchconfig(sections, values)
  }
  
  def buildLaunchconfig(sections: ArrayBuffer[String],
        values: mutable.Map[String, ArrayBuffer[String]]): Launchconfig = {
    val lines = sections.toSeq flatMap { section =>
      Seq(section) ++ values(section).toSeq      
    }
    Launchconfig(lines.mkString(System.getProperty("line.separator")))
  }
  
  def parseLaunchconfig = {
    val sections = ArrayBuffer[String]()
    val values = mutable.Map[String, ArrayBuffer[String]]()
    
    for (line <- augmentString(configstring).lines) {
      """^\[\w+\]""".r.findFirstIn(line.trim) map { s =>
        sections += s
        values(s) = ArrayBuffer[String]()
      } getOrElse {
        if (!sections.isEmpty) {
          values(sections.last) += line
        }
      }
    }
    (sections, values)
  }  
}
