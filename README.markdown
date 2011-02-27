conscript
=========

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
conscript with the name of a github project:

    cs n8han/giter8

Installs the [giter8](https://github.com/n8han/giter8) templating system.
