seq(conscriptSettings :_*)

seq(lsSettings :_*)

organization := "net.databinder.conscript"

version := "0.4.2"

name := "conscript"

scalaVersion := "2.9.2"

libraryDependencies <<= (libraryDependencies, scalaVersion) {
  (deps, sv) => deps ++ Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
    "com.github.scopt" %% "scopt" % "2.1.0",
    "org.scala-lang" % "scala-swing" % sv,
    "net.liftweb" %% "lift-json" % "2.5-M4",
    "org.slf4j" % "slf4j-jdk14" % "1.6.2"
  )
}

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class conscript.* { *; }",
  "-keep class dispatch.* { *; }",
  "-keep class org.apache.commons.logging.impl.LogFactoryImpl { *; }",
  "-keep class org.apache.commons.logging.impl.Jdk14Logger { *; }"
)

minJarPath <<= (target, version) { (t,v) =>
  t / ("conscript-" + v + ".jar")
}

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion)

buildInfoPackage := "conscript"

homepage :=
  Some(new java.net.URL("https://github.com/n8han/conscript/"))

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

licenses := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt"))

pomExtra := (
  <scm>
    <url>git@github.com:dispatch/reboot.git</url>
    <connection>scm:git:git@github.com:dispatch/reboot.git</connection>
  </scm>
  <developers>
    <developer>
      <id>n8han</id>
      <name>Nathan Hamblen</name>
      <url>http://github.com/n8han</url>
    </developer>
    <developer>
      <id>eed3si9n</id>
      <name>Eugene Yokota</name>
      <url>https://github.com/eed3si9n</url>
    </developer>
  </developers>)