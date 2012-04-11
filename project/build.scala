import sbt._
import Keys._

object ConscriptBuild extends Build {
  lazy val root =
    Project("conscript", file(".")) dependsOn(dispatch)
  lazy val dispatch = ProjectRef(
    file("../reboot"),
    "core"
  )
}
