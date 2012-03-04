addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.1.0")

resolvers += Resolver.url("sbt-plugin-releases",
  url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
