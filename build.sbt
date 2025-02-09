import Dependencies._
import ReleaseTransformations._
import scala.sys.process.Process

val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")

def buildInfo(packageName: String, v: String) = Def.settings(
  Compile / sourceGenerators += task {
    val src = s"""package ${packageName}
      |
      |private[${packageName}] object ConscriptBuildInfo {
      |  def sbtLauncherVersion: String = "${v}"
      |}
      |""".stripMargin
    val f = (Compile / sourceManaged).value / "conscript" / "ConscriptBuildInfo.scala"
    IO.write(f, src)
    Seq(f)
  },
)

lazy val commonSettings = Seq(
  publishTo := sonatypePublishToBundle.value,
  sonatypeProfileName := "org.foundweekends",
  crossSbtVersions := Seq("1.2.8")
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin, SbtProguard).
  settings(
    commonSettings,
    buildInfo(packageName = "conscript", v = sbtLauncherDeps.revision),
    libraryDependencies += sbtLauncherDeps,
    pomPostProcess := { node =>
      import scala.xml.{NodeSeq, Node}
      val rule = new scala.xml.transform.RewriteRule {
        override def transform(n: Node) = {
          if (List(
            n.label == "dependency",
            (n \ "groupId").text == sbtLauncherDeps.organization,
            (n \ "artifactId").text == sbtLauncherDeps.name,
          ).forall(identity)) {
            NodeSeq.Empty
          } else {
            n
          }
        }
      }
      new scala.xml.transform.RuleTransformer(rule).transform(node)(0)
    },
    updateLaunchconfig := {
      val mainClassName = (Compile / discoveredMainClasses).value match {
        case Seq(m) => m
        case zeroOrMulti => sys.error(s"could not found main class. $zeroOrMulti")
      }
      val s = streams.value.log
      if(isSnapshot.value) {
        s.warn(s"update launchconfig ${version.value}")
      }
      val launchconfig = s"""[app]
      |  version: ${version.value}
      |  org: ${organization.value}
      |  name: ${normalizedName.value}
      |  class: ${mainClassName}
      |[scala]
      |  version: ${scalaVersion.value}
      |[repositories]
      |  local
      |  maven-central
      |""".stripMargin
      val f = (ThisBuild / baseDirectory).value / "src/main/conscript/cs/launchconfig"
      IO.write(f, launchconfig)
      Process(Seq("git", "add", f.getAbsolutePath), (LocalRootProject / baseDirectory).value).!
      Process(Seq("git", "commit", "-m", "update " + f.getName), (LocalRootProject / baseDirectory).value).!
      f
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommandAndRemaining(s"^ plugin/scripted"),
      setReleaseVersion,
      releaseStepTask(updateLaunchconfig),
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining(s";publishSigned;^ plugin/publishSigned"),
      releaseStepCommandAndRemaining("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
    scalaVersion := "2.13.16",
    inThisBuild(List(
      organization := "org.foundweekends.conscript",
      homepage := Some(url("https://github.com/foundweekends/conscript/")),
      licenses := Seq("LGPL-3.0" -> url("https://www.gnu.org/licenses/lgpl.txt")),
      scalacOptions ++= Seq("-language:_", "-deprecation", "-Xlint", "-Xfuture"),
      developers := List(
        Developer("n8han", "Nathan Hamblen", "@n8han", url("https://github.com/n8han")),
        Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
      ),
      scmInfo := Some(ScmInfo(url("https://github.com/foundweekends/conscript"), "git@github.com:foundweekends/conscript.git"))
    )),
    name := "conscript",
    crossScalaVersions := List("2.13.16"),
    libraryDependencies ++= List(launcherInterface, scalaSwing, dispatchCore, scopt, liftJson, slf4jJdk14),
    Compile / packageBin / mainClass := Some("conscript.Conscript"),
    (Compile / packageBin / mappings) := {
      val old = (Compile / packageBin / mappings).value
      old filter { case (_, p) => p != "META-INF/MANIFEST.MF" }
    },
    (Compile / packageSrc / mappings) := {
      val old = (Compile / packageSrc / mappings).value
      old filter { case (_, p) => p != "META-INF/MANIFEST.MF" }
    },
    Proguard / proguardVersion := "7.6.1",
    Proguard / proguardOptions ++= Seq(
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
    (Proguard / proguardInputs) := {
      ((Compile / fullClasspath).value.files ++ (Runtime / fullClasspath).value.files).distinct.filter { f =>
        // This is a dependency of the launcher interface. It may not be the version of scala
        // we're using at all, and we don't want it
        f.getName != "scala-library.jar"
      }
    },
    Proguard / proguardDefaultInputFilter := None,
    Proguard / proguard / javaOptions := Seq("-Xmx2G"),
    (Proguard / proguardOutputs) := {
      (Proguard / proguardDirectory).value / ("conscript-" + version.value + ".jar") :: Nil
    },
    (Compile / proguard / artifact) := {
      val art = (Compile / proguard / artifact).value
      art.withClassifier(Some("proguard"))
    },
    addArtifact((Compile / proguard / artifact), (Proguard / proguard) map { xs => xs.head }),
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "conscript",
    publishMavenStyle := true,
    Test / publishArtifact := false,
    TaskKey[Unit]("makeSite") := {
      val output = target.value / "site"
      IO.delete(output)
      val src = (LocalRootProject / baseDirectory).value / "docs"
      val storage = pamflet.FileStorage(src, Nil)
      pamflet.Produce(storage.globalized, output)
      IO.delete(output / "offline")
      IO.delete(output / "ja" / "offline")
    },
  )

lazy val javaVmArgs: List[String] = {
  import scala.collection.JavaConverters._
  java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
}

lazy val plugin = (project in file("sbt-conscript")).
  enablePlugins(SbtPlugin).
  settings(
    commonSettings,
    buildInfo(packageName = "sbtconscript", v = Dependencies.launcherInterface.revision),
    name := "sbt-conscript",
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= javaVmArgs.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    ),
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
  )
