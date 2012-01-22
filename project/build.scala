import sbt._
import Keys._

object ConscriptBuild extends Build {
  lazy val root =
    Project("conscript", file(".")) dependsOn(dlj) aggregate(dlj)
  lazy val dlj = ProjectRef(
    uri("git://github.com/dispatch/dispatch-lift-json.git#0.9.0-alpha1"),
    "dispatch-lift-json"
  )
}
