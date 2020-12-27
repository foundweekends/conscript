import sbt._

object Dependencies {
  val launcherInterface = "org.scala-sbt" % "launcher-interface" % "1.1.6"
  val scalaSwing        = "org.scala-lang.modules" %% "scala-swing" % "2.1.1"
  val dispatchCore      = "net.databinder.dispatch" %% "dispatch-core" % "0.11.4"
  val scopt             = "com.github.scopt" %% "scopt" % "4.0.0"
  val liftJson          = "net.liftweb" %% "lift-json" % "2.6.3"
  val slf4jJdk14        = "org.slf4j" % "slf4j-jdk14" % "1.7.30"
}
