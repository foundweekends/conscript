conscriptSettings

lsSettings

organization := "net.databinder.conscript"

version := "0.4.5-SNAPSHOT"

name := "conscript"

scalaVersion := "2.11.1"

scalacOptions ++= Seq("-deprecation", "-language:_")

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
  "com.github.scopt" %% "scopt" % "3.2.0",
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.slf4j" % "slf4j-jdk14" % "1.6.2"
)

proguardSettings

ProguardKeys.proguardVersion in Proguard := "4.11"

ProguardKeys.options in Proguard ++= Seq(
  "-keep class conscript.* { *; }",
  "-keep class dispatch.* { *; }",
  "-keep class com.ning.http.client.providers.netty.** { *; }",
  "-keep class org.apache.commons.logging.impl.LogFactoryImpl { *; }",
  "-keep class org.apache.commons.logging.impl.Jdk14Logger { *; }",
  "-dontnote",
  "-dontwarn",
  "-dontobfuscate",
  "-dontoptimize"
  )

ProguardKeys.inputs in Proguard <<=
  (fullClasspath in Compile, fullClasspath in Runtime) map { (ccp, rcp) =>
    (ccp.files ++ rcp.files).distinct.filter { f =>
      // This is a dependency of the launcher interface. It may not be the version of scala
      // we're using at all, and we don't want it
      f.getName != "scala-library.jar"
    }
  }

ProguardKeys.defaultInputFilter in Proguard := None

javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx2G")

ProguardKeys.outputs in Proguard <<=
  (ProguardKeys.proguardDirectory in Proguard, version) map { (op,v) =>
    op / ("conscript-" + v + ".jar") :: Nil
  }

buildInfoSettings

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
    <url>git@github.com:n8han/conscript.git</url>
    <connection>scm:git:git@github.com:n8han/conscript.git</connection>
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
