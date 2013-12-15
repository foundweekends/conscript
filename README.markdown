![Conscript](https://github.com/n8han/conscript/raw/master/src/main/resources/conscript.png)

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
it. Firstly, `~/.conscript/boot` is used as a boot directory for
all. Program launch configurations are stored according to the github
project name and script name, such as:

    ~/.conscript/n8han/conscript/cs/launchconfig

And finally, program scripts are created in `~/bin` that reference
launch configurations, e.g. `~/bin/cs`

Installation
------------

There are two methods of installiation available.

### Linux, Mac, Windows

Download the [conscript runnable jar][jar]. On most OSes you can run
it by double-clicking, but if that doesn't work you can also run it
from the command line.

    java -jar conscript-0.4.4.jar

[jar]: https://github.com/n8han/conscript/releases/download/0.4.4-1/conscript-0.4.4-1.jar

A "splash screen" will appear during installation. Don't close it
until you see a message that `cs` was installed, or that something
went wrong.

### Linux, Mac

If you prefer, you can install conscript by piping this shell script.

    curl https://raw.github.com/n8han/conscript/master/setup.sh | sh

### Java 8

If you're using a buggy preview release of Java, then Conscript will
be buggy. Please use a release version of Java instead.
    
Use
---

The main thing you do with conscript is install/update and update
commands based on templates. Templates are stored in github projects,
which you pass into the `cs` commpand. For example, this installs the
[giter8](https://github.com/n8han/giter8) templating system:

    cs n8han/giter8

Templates specify a version of the app to use, but you can override
that by specifying an explicit version with another slash:

    cs n8han/giter8/0.2.1

Project owners may also decide to push pre-release or other alternate
templates to different branches on github. Use can tell conscript read
templates from another branch with the `--branch` or `-b` option.

    cs n8han/giter8 --branch staging

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

[cplug]: https://github.com/n8han/conscript-plugin
