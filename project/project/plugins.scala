import sbt._
object ConscriptPlugins extends Build
{
  lazy val root =
    Project("", file(".")) dependsOn(proguard)
  lazy val proguard =
    uri("git://github.com/siasia/xsbt-proguard-plugin#e898d0adb5")
}
