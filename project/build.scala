import sbt._
import Keys._

object ConscriptBuild extends Build {
  lazy val common = Defaults.defaultSettings ++ Seq(
    organization := "net.databinder",
    version := "0.3.1",
    publishTo := Some("Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
  )

  lazy val conscript =
    Project("conscript-root",
            file("."),
            settings = common).aggregate(app, plugin)
  lazy val app: Project =
    Project("conscript", file("app"), settings = common)
  lazy val plugin: Project =
    Project("conscript-plugin", file("plugin"), settings = common)
}
