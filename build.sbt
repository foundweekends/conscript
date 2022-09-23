import Dependencies._
import com.typesafe.sbt.SbtGit.{git, GitKeys}
import com.typesafe.sbt.git.GitRunner
import ReleaseTransformations._

lazy val pushSiteIfChanged = taskKey[Unit]("push the site if changed")

val updateLaunchconfig = TaskKey[File]("updateLaunchconfig")

lazy val commonSettings = Seq(
  publishTo := sonatypePublishToBundle.value,
  sonatypeProfileName := "org.foundweekends",
  crossSbtVersions := Seq("1.2.8")
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin, PamfletPlugin, SbtProguard, GhpagesPlugin).
  settings(
    commonSettings,
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
      val r = GitKeys.gitRunner.value
      r("add", f.getAbsolutePath)((LocalRootProject / baseDirectory).value, s)
      r("commit", "-m", "update " + f.getName)((LocalRootProject / baseDirectory).value, s)
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
    scalaVersion := "2.13.9",
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
    crossScalaVersions := List("2.13.9"),
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
    Proguard / proguardVersion := "7.2.2",
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
    (Pamflet / sourceDirectory) := { (LocalRootProject / baseDirectory).value / "docs" },
    // GitKeys.gitBranch in ghkeys.updatedRepository := Some("gh-pages"),
    // This task is responsible for updating the master branch on some temp dir.
    // On the branch there are files that was generated in some other ways such as:
    // - CNAME file
    //
    // This task's job is to call "git rm" on files and directories that this project owns
    // and then copy over the newly generated files.
    ghpagesSynchLocal := {
      // sync the generated site
      val repo = ghpagesUpdatedRepository.value
      val s = streams.value
      val r = GitKeys.gitRunner.value
      gitRemoveFiles(repo, (repo * "*.html").get.toList, r, s)
      val mappings =  for {
        (file, target) <- siteMappings.value
      } yield (file, repo / target)
      IO.copy(mappings)
      repo
    },
    pushSiteIfChanged := (Def.taskDyn {
      val repo = (LocalRootProject / baseDirectory).value
      val r = GitKeys.gitRunner.value
      val s = streams.value
      val changed = gitDocsChanged(repo, r, s.log)
      if (changed) {
        ghpagesPushSite
      } else {
        Def.task {
          s.log.info("skip push site")
        }
      }
    }).value,
    git.remoteRepo := "git@github.com:foundweekends/conscript.git"
  )

lazy val javaVmArgs: List[String] = {
  import scala.collection.JavaConverters._
  java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toList
}

lazy val plugin = (project in file("sbt-conscript")).
  enablePlugins(SbtPlugin).
  settings(
    commonSettings,
    name := "sbt-conscript",
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= javaVmArgs.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Dsbt.log.noformat").exists(a.startsWith)
    ),
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value)
  )

def gitRemoveFiles(dir: File, files: List[File], git: GitRunner, s: TaskStreams): Unit = {
  if(!files.isEmpty)
    git(("rm" :: "-r" :: "-f" :: "--ignore-unmatch" :: files.map(_.getAbsolutePath)) :_*)(dir, s.log)
  ()
}

def gitDocsChanged(dir: File, git: GitRunner, log: Logger): Boolean =
  {
    // git diff --shortstat HEAD^..HEAD docs
    val range = sys.env.get("TRAVIS_COMMIT_RANGE") match {
      case Some(x) => x
      case _       => "HEAD^..HEAD"
    }
    val stat = git(("diff" :: "--shortstat" :: range :: "--" :: "docs" :: Nil) :_*)(dir, log)
    stat.trim.nonEmpty
  }
