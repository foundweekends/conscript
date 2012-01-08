import sbt._
object Appy extends Build
{
  lazy val root =
    Project("conscript", file(".")) dependsOn(reboot)
  lazy val reboot =
    uri("../dispatch-lift-json")
}
