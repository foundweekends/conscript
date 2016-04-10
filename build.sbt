import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.foundweekends.conscript",
      version := "0.5.0-SNAPSHOT",
      scalaVersion := "2.11.8",
      homepage := Some(url("https://github.com/foundweekends/conscript/"))
    )),
    // conscriptSettings,
    // lsSettings,
    name := "conscript",
    libraryDependencies ++= List(launcherInterface, scalaSwing, dispatchCore, scopt, liftJson, slf4jJdk14),
    proguardSettings,
    ProguardKeys.options in Proguard ++= Seq(
      "-keep class conscript.* { *; }",
      "-keep class dispatch.* { *; }",
      "-keep class com.ning.http.util.** { *; }",
      "-keep class com.ning.http.client.providers.netty.** { *; }",
      "-keep class org.apache.commons.logging.impl.LogFactoryImpl { *; }",
      "-keep class org.apache.commons.logging.impl.Jdk14Logger { *; }",
      "-dontnote",
      "-dontwarn",
      "-dontobfuscate",
      "-dontoptimize"
      ),
    ProguardKeys.inputs in Proguard <<=
      (fullClasspath in Compile, fullClasspath in Runtime) map { (ccp, rcp) =>
        (ccp.files ++ rcp.files).distinct.filter { f =>
          // This is a dependency of the launcher interface. It may not be the version of scala
          // we're using at all, and we don't want it
          f.getName != "scala-library.jar"
        }
      },
    ProguardKeys.defaultInputFilter in Proguard := None,
    javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx2G"),
    ProguardKeys.outputs in Proguard <<=
      (ProguardKeys.proguardDirectory in Proguard, version) map { (op,v) =>
        op / ("conscript-" + v + ".jar") :: Nil
      },
    buildInfoSettings,
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "conscript",
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    licenses := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt")),
    pomExtra := (
      <scm>
        <url>git@github.com:foundweekends/conscript.git</url>
        <connection>scm:git:git@github.com:foundweekends/conscript.git</connection>
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
  )
