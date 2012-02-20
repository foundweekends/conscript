import sbt._
object Appy extends Build
{
  lazy val root =
    Project("conscript", file(".")) dependsOn(reboot)
  lazy val reboot =
    uri("git://github.com/dispatch/dispatch-lift-json.git#0.9.0-alpha2")
}
