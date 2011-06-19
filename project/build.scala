import sbt._

object ConscriptBuild extends Build {
  lazy val conscript =
    Project("conscript-root", file(".")) aggregate(app, plugin)
  lazy val app: Project =
    Project("conscript", file("app"),
            delegates = conscript :: Nil)
  lazy val plugin: Project =
    Project("conscript-plugin", file("plugin"),
            delegates = conscript :: Nil)
}
