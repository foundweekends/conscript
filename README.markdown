conscript: Scala at your command
=========================

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

    java -jar conscript-0.3.0.jar

[jar]: https://github.com/downloads/n8han/conscript/conscript-0.3.0.jar

After that you'll have `cs` or `cs.bat` (depending on your OS) in a
`bin` directory under your home directory. It is up to you to get that
onto your executable search path, if it is not already.

### Linux, Mac

If you prefer, you can install conscript by piping this shell script.

    curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
    
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

Conscripting
------------

We hope you'll make your own programs that use conscript. The
conscript-plugin for sbt makes these easier to build and test, and
there's a [conscript.g8 template][csg8] to get you started. (See above
for g8 install.)

[csg8]: https://github.com/n8han/conscript.g8#readme
