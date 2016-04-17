---
out: setup.html
---

  [runnable]: https://dl.bintray.com/foundweekends/maven-releases/org/foundweekends/conscript/conscript_2.11/$version$/conscript_2.11-$version$-proguard.jar
  [ps]: https://raw.githubusercontent.com/foundweekends/conscript/master/setup.ps1

Installing conscript
--------------------

### Set up environment variables

Depending on your OS the format might be different, but set up the following three environment variables.

    export CONSCRIPT_HOME="\$HOME/.conscript"
    export CONSCRIPT_OPTS="-XX:MaxPermSize=512M -Dfile.encoding=UTF-8"
    export PATH=\$CONSCRIPT_HOME/bin:\$PATH

- `CONSCRIPT_HOME` is where Conscript will download various files.
- `CONSCRIPT_OPTS` is JVM arguments passed on to the apps that you installed using Conscript.
- By default Conscript will create the launching script for the apps in `\$CONSCRIPT_HOME/bin`. Optionally you can change this location using `\$CONSCRIPT_BIN` for example to `~/bin/`
- `PATH` is your OS's path variable. Add `\$CONSCRIPT_HOME/bin` or `\$CONSCRIPT_BIN` to the path.

There are three methods of installation available

<!- test -->

### Cross platform

Download the conscript [runnable jar][runnable]. On most OSes you can run it by double-clicking, but if that doesn't work you can also run it from the command line.

```
\$ java -jar conscript_2.11-$version$-proguard.jar
```

A "splash screen" will appear during installation. Don't close it until you see a message that `cs` was installed, or that something went wrong.

### Linux, Mac

If you prefer, you can install conscript by piping this shell script.

```
curl https://raw.githubusercontent.com/foundweekends/conscript/master/setup.sh | sh
```

### Windows

If you prefer, you can download the PowerShell [script][ps] and run it.
