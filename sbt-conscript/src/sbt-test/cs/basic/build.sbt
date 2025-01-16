enablePlugins(ConscriptPlugin)

version := "0.1.0-SNAPSHOT"

libraryDependencies += "org.scala-sbt" %% "io" % "1.1.0"

scalaVersion := "2.13.16"

organization := "com.example"

name := "hello"

TaskKey[Unit]("check") := {
  val a = IO.read(file("a"))
  assert(a == "b", a)
}
