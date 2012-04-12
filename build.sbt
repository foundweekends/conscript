seq(conscriptSettings :_*)

organization := "net.databinder.conscript"

version := "0.4.0"

name := "conscript"

publishTo := Some("Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalaVersion := "2.9.1"

libraryDependencies <<= (libraryDependencies, scalaVersion) {
  (deps, sv) => deps ++ Seq(
    "net.databinder.dispatch" %% "core" % "0.9.0-alpha5",
    "com.github.scopt" %% "scopt" % "1.1.2",
    "org.scala-lang" % "scala-swing" % sv,
    "net.liftweb" %% "lift-json" % "2.4-RC1",
    "org.slf4j" % "slf4j-jdk14" % "1.6.2"
  )
}

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class conscript.* { *; }",
  "-keep class org.apache.commons.logging.impl.LogFactoryImpl { *; }",
  "-keep class org.apache.commons.logging.impl.Jdk14Logger { *; }"
)

minJarPath <<= (target, version) { (t,v) =>
  t / ("conscript-" + v + ".jar")
}

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[Scoped](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "conscript"
