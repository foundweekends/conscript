---
out: making-app.html
---

  [sbtlauncher]: https://www.scala-sbt.org/1.x/docs/Sbt-Launcher.html
  [scopt]: https://github.com/scopt/scopt

Making a conscripted app
------------------------

We hope you'll make your own apps that use conscript.

### ConscriptPlugin

The `ConscriptPlugin` makes these easier to build and test.
Add this to the following `project/conscript.sbt`:

```scala
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "$version$")
```

Next enable `ConscriptPlugin` on the app subproject in `build.sbt`:

```
lazy val root = (project in file(".")).
  enablePlugins(ConscriptPlugin).
  settings(
    // other settings here
  )
```

The plugin brings in a "provided" dependency to sbt launcher-interface.

### Entry point

Make the entry point to your app by implementing `xsbti.AppMain`.

```scala
package example

class HelloApp extends xsbti.AppMain {
  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    // get the version of Scala used to launch the application
    val scalaVersion = configuration.provider.scalaProvider.version

    // Print a message and the arguments to the application
    println("Hello world!  Running Scala " + scalaVersion)
    configuration.arguments.foreach(println)

    new Exit(0)
  }
  class Exit(val code: Int) extends xsbti.Exit
}
```

After one or two command line options, you might want to take a look at [scopt][scopt] to do commandline parsing.

### launchconfig

Next, add your sbt `launchconfig` file to `src/main/conscript/XYZ/launchconfig` (substitue `XYZ` with your script name such as `g8` and `cs`):

```
[app]
  version: 0.1.0
  org: com.example
  name: hello
  class: example.HelloApp
[scala]
  version: 2.11.12
[repositories]
  local
  maven-central
```

To learn more about the `launchconfig`, see [sbt Launcher][sbtlauncher] section of the sbt Reference Manual.

### csRun

You can test the app by calling `csRun XYZ` command.
