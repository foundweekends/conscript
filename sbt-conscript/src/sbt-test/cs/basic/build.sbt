enablePlugins(ConscriptPlugin)

version := "0.1.0-SNAPSHOT"

libraryDependencies += "org.scala-sbt" %% "io" % "1.0.0-M12"

scalaVersion := "2.11.11"

organization := "com.example"

name := "hello"

TaskKey[Unit]("check") := {
  val a = IO.read(file("a"))
  assert(a == "b", a)
}
