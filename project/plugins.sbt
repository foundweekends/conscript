addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-proguard" % "0.3.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
