import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn( conscript )
  lazy val conscript = uri("git://github.com/n8han/conscript-plugin.git#0.1.2")
}
