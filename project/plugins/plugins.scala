import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  // publish release notes to implicit.ly
  val t_repo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
  val posterous = "net.databinder" % "posterous-sbt" % "0.1.5"
  // publish source to sourced.implicit.ly
  val sxr_publish = "net.databinder" % "sxr-publish" % "0.2.0"
  val conscript = "net.databinder" % "conscript-plugin" % "0.2.2-SNAPSHOT"
}
