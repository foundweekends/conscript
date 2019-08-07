import sbt._

object Dependencies {
  val launcherInterface = "org.scala-sbt" % "launcher-interface" % "1.1.1"
  val scalaSwing        = "org.scala-lang.modules" %% "scala-swing" % "2.1.1"
  val dispatchCore      = "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
  val scopt             = "com.github.scopt" %% "scopt" % "3.7.1"
  val liftJson          = "net.liftweb" %% "lift-json" % "2.6.3"
  val slf4jJdk14        = "org.slf4j" % "slf4j-jdk14" % "1.7.27"
}
