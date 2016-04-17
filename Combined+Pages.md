
Conscript
=========

![Conscript](files/conscript.png)

Conscript is a distribution mechanism for Scala apps using Github and Maven repositories as the infrastructure. You can use it to install and update apps similar to APT or Home Brew.

It does less than you think, because the sbt launcher does more than you think. More on that later.


  [runnable]: https://dl.bintray.com/foundweekends/maven-releases/org/foundweekends/conscript/conscript_2.11/0.5.0/conscript_2.11-0.5.0-proguard.jar
  [ps]: https://raw.githubusercontent.com/foundweekends/conscript/master/setup.ps1

Installing conscript
--------------------

### Set up environment variables

Depending on your OS the format might be different, but set up the following three environment variables.

    export CONSCRIPT_HOME="$HOME/.conscript"
    export CONSCRIPT_OPTS="-XX:MaxPermSize=512M -Dfile.encoding=UTF-8"
    export PATH=$CONSCRIPT_HOME/bin:$PATH

- `CONSCRIPT_HOME` is where Conscript will download various files.
- `CONSCRIPT_OPTS` is JVM arguments passed on to the apps that you installed using Conscript.
- By default Conscript will create the launching script for the apps in `$CONSCRIPT_HOME/bin`. Optionally you can change this location using `$CONSCRIPT_BIN` for example to `~/bin/`
- `PATH` is your OS's path variable. Add `$CONSCRIPT_HOME/bin` or `$CONSCRIPT_BIN` to the path.

There are three methods of installation available

<!- test test -->

### Cross platform

Download the conscript [runnable jar][runnable]. On most OSes you can run it by double-clicking, but if that doesn't work you can also run it from the command line.

```
$ java -jar conscript_2.11-0.5.0-proguard.jar
```

A "splash screen" will appear during installation. Don't close it until you see a message that `cs` was installed, or that something went wrong.

### Linux, Mac

If you prefer, you can install conscript by piping this shell script.

```
curl https://raw.githubusercontent.com/foundweekends/conscript/master/setup.sh | sh
```

### Windows

If you prefer, you can download the PowerShell [script][ps] and run it.


Using conscript
---------------

The main thing you do with conscript is install and update commands based on `launchconfig`.
`launchconfig` files are stored in Github projects, which you pass into the `cs` command.
For example, this installs the giter8 templating system:

```
$ cs foundweekends/giter8
```

`launchconfig`s specify a version of the app to use, but you can override that by specifying an explicit version with another slash:

```
$ cs foundweekends/giter8/0.2.1
```

Project owners may also decide to push pre-release or other alternate `launchconfig` to different branches on Github. Use can tell conscript read `launchconfig`s from another branch with the `--branch` or `-b` option.

```
$ cs foundweekends/giter8 --branch staging
```

### Cleaning boot directory

If at some point your conscript boot directory contains stale/suspect artifacts such as snapshot releases, you can clean it:

```
$ cs --clean-boot
```

When you next run any conscripted app (such as `cs`) it will fetch its cleaned dependencies back into the shared boot directory; generally it only has to look as far as the local ivy cache to find them.


How does conscript work?
------------------------

Conscript does *less* than you think, because the sbt launcher does more than
you think. What?

<br>**What conscript does:**

- Queries a Github project for `launchconfig`s (launcher configruations)
- Copies `launchconfig`s to your local filesystem, with a personalized boot path
- Creates scripts to execute the launch configurations

**What sbt launcher does:**

- Reads a given `launchconfig`
- Fetches needed dependencies on the first run
- Uses the same Ivy cache as sbt itself

So conscript just assumes a convention and helps you adhere to
it.
Suppose you've configured `$CONSCRIPT_HOME` to `$HOME/.conscript`.

Then `$CONSCRIPT_HOME/boot` is used as a boot directory for
all.
App `launchconfig`s are stored according to the Github
project name and the script name, such as:

    $CONSCRIPT_HOME/foundweekends/conscript/cs/launchconfig

And finally, shell scripts are created in `$CONSCRIPT_BIN`
(default: `$CONSCRIPT_HOME/bin`) that reference `launchconfig`s,
for example, `~/.conscript/bin/cs`. This is how the shell script looks like on Mac:

```
#!/bin/sh
java -jar /Users/foo/.conscript/sbt-launch.jar \
  @/Users/foo/.conscript/foundweekends/conscript/cs/launchconfig "$@"
```

Once the shell script is created, now it's up to the sbt launcher to carry out the actual work of fetching artifacts and running the app.


[oauth]: http://developer.github.com/v3/oauth/#create-a-new-authorization
[tokens]: https://github.com/settings/applications

Private repositories
--------------------

Conscript supports **private github repos** using github's oauth flow
[for non-web apps][oauth].

To authenticate and store a permanent token, use the `--auth` parameter:

```
$ cs --auth yourname:yourpass
```

This stores an access token in `~/.conscript/config` which is used for
all future `cs` invocations. You can revoke tokens at any time in your
[Github account settings][tokens].

The sbt launcher can access **private Maven/Ivy repos** just as sbt
itself can. Specify a credentials properties file, such as
`~/.ivy2/.credentials`, in the `sbt.boot.credentials` JVM property or
`SBT_CREDENTIALS` environment variable. The launcher will use these
credentials when accessing protected resources in the specified realm.


  [sbtlauncehr]: http://www.scala-sbt.org/0.13/docs/Sbt-Launcher.html
  [scopt]: https://github.com/scopt/scopt

Making a conscripted app
------------------------

We hope you'll make your own apps that use conscript.

### ConscriptPlugin

The `ConscriptPlugin` makes these easier to build and test.
Add this to the following `project/conscript.sbt`:

```scala
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.0")
```

Next enable `ConscriptPlugin` on the app subproject in `build.sbt`:

```
lazy val root = (project in file(".")).
  eanble(ConscriptPlugin).
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
  version: 2.11.8
[repositories]
  local
  maven-central
  sonatype-releases: https://oss.sonatype.org/content/repositories/releases/
```

To learn more about the `launchconfig`, see [sbt Launcher][sbtlauncher] section of the sbt Reference Manual.

### Bintray

You can add arbitray repositories to the repositories section, including a Bintray Maven repo.
Here is the Bintray repo for foundweekends for example.

```
  foundweekends-maven-releases: https://dl.bintray.com/foundweekends/maven-releases/
```

### csRun

You can test the app by calling `csRun XYZ` command.


  [search]: https://github.com/search?o=desc&q=+path%3Asrc%2Fmain%2Fconscript++org&ref=searchresults&s=indexed&type=Code&utf8=%E2%9C%93
  [scriptrunner]: http://www.scala-sbt.org/0.13/docs/Scripts.html

Conscripted apps
----------------

Because conscipt uses a known path `/src/main/conscript/` on Github, we can [search][search] the Github repos to discover conscripted apps.

Let us know if you've written a conscripted app.

- [foundweekends/conscript](https://github.com/foundweekends/conscript). conscript itself is a conscripted app.
- [foundweekends/giter8](https://github.com/foundweekends/giter8). giter8 is a tool to apply templated hosted on Github.
- [foundweekends/pamflet](https://github.com/foundweekends/pamflet). A publishing application for short texts.
- [sbt/sbt](https://github.com/sbt/sbt). sbt provides apps for `sbt`, `scalas` ([sbt Script runner][scriptrunner]), and `screpl`.
- [n8han/herald](https://github.com/n8han/herald). Tell the world about your latest software release.

- [softprops/picture-show](https://github.com/softprops/picture-show). slip and slide picture shows for the web.
- [softprops/unplanned](https://github.com/softprops/unplanned). instant http.
- [softprops/gooose](https://github.com/softprops/gooose). when I tap you, you shall become the goose.
- [softprops/spakle](https://github.com/softprops/spakle). ▁▇▁▄▃▂▄▄▆▆▅▃▅▁▂ just like nyc's skyline.
- [softprops/pj](https://github.com/softprops/pj). a pajama party for your json strings and streamers.
- [softprops/gist](https://github.com/softprops/gist). it's like git with an s between the i and t.
- [softprops/chrome-pilot](https://github.com/softprops/chrome-pilot). chrome is your airship. your helm is the command line.
- [softprops/pb](https://github.com/softprops/pb). cat once, stash anywhere
- [softprops/hostclub](https://github.com/softprops/hostclub). your host mappings are all up in the club

- [philcali/cronish-app](https://github.com/philcali/cronish-app). An app to define sh command interval execution.
- [philcali/monido](https://github.com/philcali/monido). Scala monitoring service.
- [philcali/scagen](https://github.com/philcali/scagen). A flexible site generating tool.
- [philcali/saydo](https://github.com/philcali/saydo). Weekly todo template in markdown.
- [philcali/lmxml](https://github.com/philcali/lmxml). The Light Markup to XML.
- [philcali/puppet-master](https://github.com/philcali/puppet-master). Scripted LMXML HTTP client (puppet).
- [philcali/robot-vision](https://github.com/philcali/robot-vision). Embedded server to allow remote control over desktop.
- [philcali/spdf](https://github.com/philcali/spdf). Quickly create PDFs on the command-line.
- [philcali/webdir](https://github.com/philcali/webdir). Start an embedded web server to download and upload file from a directory.

- [eed3si9n/scalaxb](https://github.com/eed3si9n/scalaxb). an XML data binding tool for Scala.
- [eed3si9n/doctrine](https://github.com/eed3si9n/doctrine). an app to download Scala docs.
- [todesking/jcon](https://github.com/todesking/jcon). A generic JDBC console.
- [todesking/nyandoc](https://github.com/todesking/nyandoc). Javadoc/Scaladoc to markdown converter.
- [tototoshi/hatenacala](https://github.com/tototoshi/hatenacala). A commandline tool for Hatena Diary.
- [tototoshi/mvnsearch](https://github.com/tototoshi/mvnsearch). A tool for searching Java/Scala library.
- [cb372/scala-ascii-art](https://github.com/cb372/scala-ascii-art). A simple Ascii art generator.
- [arosien/sniff](https://github.com/arosien/sniff). Generate "bad code smells" specs2 specifications.
- [stackmob/lucid](https://github.com/stackmob/lucid). The StackMob provisioning API test tool.
- [katlex/jscs](https://github.com/katlex/jscs). Javascript compilation service.
- [judu/jetdoc](https://github.com/judu/jetdoc). Get and serve javadoc on demand.
- [numa08/ShadowDancer](https://github.com/numa08/ShadowDancer). Data transfer tool for Kagemai.
- [tkawachi/hocon2json](https://github.com/tkawachi/hocon2json). Convert from Typesafe config (including HOCON) to JSON.
- [paulmr/quiptic-to-puz](https://github.com/paulmr/quiptic-to-puz). Unofficial scraper for some specific crosswords.
- [alexarchambault/artifact-app](https://github.com/alexarchambault/artifact-app). Run app from Ivy/maven modules.
- [kazuhito-m/mindmeister2trello-importer](https://github.com/kazuhito-m/mindmeister2trello-importer). Mindmeister to Trello importer.
