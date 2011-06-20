// libraryDependencies += "net.databinder" %% "conscript-plugin" % "0.3.0-SNAPSHOT"

resolvers += "Proguard plugin repo" at "http://siasia.github.com/maven2"

libraryDependencies <+= sbtVersion("com.github.siasia" %% "xsbt-proguard-plugin" % _)