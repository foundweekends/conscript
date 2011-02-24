conscript
=========

This is a tool for installing and updating Scala software programs. It
does *less* than you think, because the sbt launcher does more than
you think. What?

**conscript...**

* Queries github for a project's current launch configuration
* Writes this to your local filesystem, with a personalized boot path
* Creates a script to execute this launch configuration

**sbt-launcher...**

* Reads the given launch configuration
* Fetches needed dependencies on first run
* Uses the same ivy cache as sbt itself

conscript assumes a convention and helps you apply it. Firstly,
`~/.conscript/boot` is used as a boot directory for all programs, to
save space. Program launch configurations are stored according to
their project's name on github, e.g. `~/.conscript/n8han/conscript`. 
And finally, program scripts are created in `~/bin`.

Installation
------------

    curl https://github.com/n8han/conscript/raw/master/setup.sh | sh

Use
---

The only command currently is install/update, initiated by calling
conscript with the name of a github project:

    cs n8han/giter8

Installs the [giter8](https://github.com/n8han/giter8) templating system.
