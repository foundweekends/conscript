addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.15")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("com.github.xuwei-k" % "sbt-proguard" % "0.5.1")
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
