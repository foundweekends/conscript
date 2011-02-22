import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) 
    with posterous.Publish with sxr.Publish {
  val launch = "org.scala-tools.sbt" % "launcher-interface" % "0.7.4" % "provided"
  val dj = "net.databinder" %% "dispatch-lift-json" % "0.8.0.Beta3"
  val dn = "net.databinder" %% "dispatch-http" % "0.8.0.Beta3"

  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
}
