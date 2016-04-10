![Conscript](https://github.com/foundweekends/conscript/raw/master/src/main/resources/conscript.png)

This is a tool for installing and updating Scala software programs. It
does *less* than you think, because the sbt launcher does more than
you think. What?

**conscript...**

* Queries a github project for launch configurations
* Writes these to your local filesystem, with a personalized boot path
* Creates scripts to execute the launch configurations

**sbt-launcher...**

* Reads a given launch configuration
* Fetches needed dependencies on first run
* Uses the same ivy cache as sbt itself

So conscript just assumes a convention and helps you adhere to
it. First, you need to configure `$CONSCRIPT_HOME`
(for example `$HOME/.conscript`).
Then`$CONSCRIPT_HOME/boot` is used as a boot directory for
all. Program launch configurations are stored according to the github
project name and script name, such as:

    $CONSCRIPT_HOME/foundweekends/conscript/cs/launchconfig

And finally, program scripts are created in `$CONSCRIPT_HOME/bin` that
reference launch configurations, e.g. `$CONSCRIPT_HOME/bin/cs`

Installation
------------

Put this in your start up shell script:

```
export CONSCRIPT_HOME="$HOME/.conscript"
export CONSCRIPT_OPTS="-XX:MaxPermSize=512M -Dfile.encoding=UTF-8"
export PATH=$CONSCRIPT_HOME/bin:$PATH
```

There are two methods of installation available.

### Linux, Mac, Windows

Download the [conscript runnable jar][jar]. On most OSes you can run
it by double-clicking, but if that doesn't work you can also run it
from the command line.

    java -jar conscript_2.11-0.5.0-proguard.jar

[jar]: https://dl.bintray.com/foundweekends/maven-releases/org/foundweekends/conscript/conscript_2.11/0.5.0/conscript_2.11-0.5.0-proguard.jar

A "splash screen" will appear during installation. Don't close it
until you see a message that `cs` was installed, or that something
went wrong.

### Linux, Mac

If you prefer, you can install conscript by piping this shell script.

    curl https://raw.githubusercontent.com/foundweekends/conscript/master/setup.sh | sh

Use
---

The main thing you do with conscript is install/update and update
commands based on templates. Templates are stored in github projects,
which you pass into the `cs` command. For example, this installs the
[giter8](https://github.com/foundweekends/giter8) templating system:

    cs foundweekends/giter8

Templates specify a version of the app to use, but you can override
that by specifying an explicit version with another slash:

    cs foundweekends/giter8/0.2.1

Project owners may also decide to push pre-release or other alternate
templates to different branches on github. Use can tell conscript read
templates from another branch with the `--branch` or `-b` option.

    cs foundweekends/giter8 --branch staging

And lastly, if at some point your conscript boot directory contains
stale/suspect artifacts such as snapshot releases, you can clean it:

    cs --clean-boot

When you next run any conscript app (such as `cs`) it will fetch its
cleaned dependencies back into the shared boot directory; generally it
only has to look as far as the local ivy cache to find them.

Private Repositories
--------------------

Conscript supports **private github repos** using github's oauth flow
[for non-web apps][oauth].

[oauth]: http://developer.github.com/v3/oauth/#create-a-new-authorization

To authenticate and store a permanent token, use the `--auth` parameter:

    cs --auth yourname:yourpass

This stores an access token in `~/.conscript/config` which is used for
all future `cs` invocations. You can revoke tokens at any time in your
[github account settings][tokens].

[tokens]: https://github.com/settings/applications

The sbt 0.11+ launcher can access **private Maven/Ivy repos** just as sbt
itself can. Specify a credentials properties file, such as
`~/.ivy2/.credentials`, in the `sbt.boot.credentials` JVM property or
`SBT_CREDENTIALS` environment variable. The launcher will use these
credentials when accessing protected resources in the specified realm.

Mailing List
------------

Join the [Conscript mailing list][list] to ask questions and stay up to
date on the project.

[list]: https://groups.google.com/forum/?hl=en#!forum/conscript-scala

Conscripting
------------

We hope you'll make your own programs that use conscript. The
[conscript-plugin][cplug] makes these easier to build and test.

[cplug]: https://github.com/foundweekends/conscript-plugin
