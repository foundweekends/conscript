import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.foundweekends.conscript",
      version := "0.5.0-SNAPSHOT",
      scalaVersion := "2.11.8",
      homepage := Some(url("https://github.com/foundweekends/conscript/")),
      bintrayOrganization := Some("foundweekends"),
      bintrayRepository := "maven-releases",
      bintrayReleaseOnPublish := false,
      bintrayPackage := "conscript",
      licenses := Seq("LGPL-3.0" -> url("http://www.gnu.org/licenses/lgpl.txt")),
      developers := List(
        Developer("n8han", "Nathan Hamblen", "@n8han", url("http://github.com/n8han")),
        Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
      ),
      scmInfo := Some(ScmInfo(url("https://github.com/foundweekends/conscript"), "git@github.com:foundweekends/conscript.git"))
    )),
    name := "conscript",
    libraryDependencies ++= List(launcherInterface, scalaSwing, dispatchCore, scopt, liftJson, slf4jJdk14),
    bintrayPackage := (bintrayPackage in ThisBuild).value,
    bintrayRepository := (bintrayRepository in ThisBuild).value,
    mainClass in (Compile, packageBin) := Some("conscript.Conscript"),
    mappings in (Compile, packageBin) := {
      val old = (mappings in (Compile, packageBin)).value
      old filter { case (_, p) => p != "META-INF/MANIFEST.MF" }
    },
    mappings in (Compile, packageSrc) := {
      val old = (mappings in (Compile, packageSrc)).value
      old filter { case (_, p) => p != "META-INF/MANIFEST.MF" }
    },
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
    artifact in (Compile, ProguardKeys.proguard) := {
      val art = (artifact in (Compile, ProguardKeys.proguard)).value
      art.copy(`classifier` = Some("proguard"))
    },
    addArtifact(artifact in (Compile, ProguardKeys.proguard), (ProguardKeys.proguard in Proguard) map { xs => xs.head }),
    buildInfoSettings,
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "conscript",
    publishMavenStyle := true,
    publishArtifact in Test := false
  )
