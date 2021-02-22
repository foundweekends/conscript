addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-proguard" % "0.4.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.6.1")
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
