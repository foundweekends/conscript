package conscript

trait ConfigEntry {
  val section: String
  val key: String
  val value: String
}

case class ConfigVersion(version: String) extends ConfigEntry {
  val section = "[app]"
  val key = "version"
  val value =
    if (version startsWith "/") version drop 1
    else version  
}

case class ConfigBootDir(value: String) extends ConfigEntry {
  val section = "[boot]"
  val key = "directory"
}

case object ConfigVerbose extends ConfigEntry {
  val section = "[log]"
  val key = "level"
  val value = "debug"
}

case class Launchconfig(configstring: String) {  
  override def toString = configstring
  
  def update(e: ConfigEntry): Launchconfig =
    updateValue(e.section, e.key, e.value)
  
  def updateValue(section: String, key: String, v: String): Launchconfig = {
    val (sections, values) = parseLaunchconfig
    val entry = "  %s: %s".format(key, v)
    sections find {_ == section} map { _ =>
      values(section) find { _.trim.startsWith(key + ":") } map { _ =>
        values(section) = values(section) map {
          case x if x.trim.startsWith(key + ":") => entry
          case x => x
        }
      } getOrElse { values(section) += entry }
    } getOrElse {
      sections += section
      values(section) = scala.collection.mutable.ArrayBuffer[String](entry)       
    }
    buildLaunchconfig(sections, values)
  }
  
  def buildLaunchconfig(sections: scala.collection.mutable.ArrayBuffer[String],
        values: scala.collection.mutable.Map[String, scala.collection.mutable.ArrayBuffer[String]]): Launchconfig = {
    val lines = sections.toSeq flatMap { section =>
      Seq(section) ++ values(section).toSeq      
    }
    Launchconfig(lines.mkString(System.getProperty("line.separator")))
  }
  
  def parseLaunchconfig = {
    val sections = scala.collection.mutable.ArrayBuffer[String]()
    val values = scala.collection.mutable.Map[String, scala.collection.mutable.ArrayBuffer[String]]()
    
    for (line <- configstring.lines) {
      """\[\w+\]""".r.findFirstIn(line.trim) map { s =>
        sections += s
        values(s) = scala.collection.mutable.ArrayBuffer[String]()
      } getOrElse {
        if (!sections.isEmpty) {
          values(sections.last) += line
        }
      }
    }
    (sections, values)
  }  
}
