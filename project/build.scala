import sbt._
object Appy extends Build
{
  lazy val root =
    Project("conscript", file(".")) dependsOn(reboot)
  lazy val reboot =
    uri("git://github.com/dispatch/dispatch-lift-json.git#e5d565e9e07d0b8935ae9c3fb90cf57fd9c893aa")
}
