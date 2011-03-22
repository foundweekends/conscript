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

    curl https://github.com/n8han/conscript/raw/master/setup.sh | sh
    
You'll probably want to add `~/bin` to your `$PATH`, if it's not
already there.

Use
---

The only command currently is install/update, initiated by calling
conscript with the name of a github project. This installs the
[giter8](https://github.com/n8han/giter8) templating system:

    cs n8han/giter8

If at some point your conscript boot directory contains stale
artifacts such as snapshot releases, you can clean it:

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
