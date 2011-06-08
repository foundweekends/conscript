import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) 
    with posterous.Publish
    with conscript.Harness {
  lazy val app = project("app", 
                         "conscript", 
                         new DefaultProject(_) with sxr.Publish with assembly.AssemblyBuilder {
    val launch = "org.scala-tools.sbt" % "launcher-interface" % "0.7.4" % "provided"
    val dj = "net.databinder" %% "dispatch-lift-json" % "0.8.1"
    val dn = "net.databinder" %% "dispatch-http" % "0.8.1"
    override val assemblyJarName = "conscript-setup-%s.jar" format (version)
    val scopt = "com.github.scopt" %% "scopt" % "1.1.1"
  })
  lazy val plugins = project("plugin", "conscript plugin", new PluginProject(_))
  override def postTitle(vers: String) = "conscript %s".format(vers)

  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}
