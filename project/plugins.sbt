addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.5")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
