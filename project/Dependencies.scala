import sbt._

object Dependencies {
  val sbtLauncherDeps   = "org.scala-sbt" % "launcher" % "1.4.4" % "test"
  val launcherInterface = "org.scala-sbt" % "launcher-interface" % "1.4.4"
  val scalaSwing        = "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
  val dispatchCore      = "net.databinder.dispatch" %% "dispatch-core" % "0.13.4"
  val scopt             = "com.github.scopt" %% "scopt" % "4.1.0"
  val liftJson          = "net.liftweb" %% "lift-json" % "2.6.3"
  val slf4jJdk14        = "org.slf4j" % "slf4j-jdk14" % "1.7.36"
}
