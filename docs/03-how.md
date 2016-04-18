---
out: how.html
---

How does conscript work?
------------------------

Conscript does *less* than you think, because the sbt launcher does more than
you think. What?

<br>**What conscript does:**

- Queries a Github project for `launchconfig`s (launcher configurations)
- Copies `launchconfig`s to your local filesystem, with a personalized boot path
- Creates scripts to execute the launch configurations

**What sbt launcher does:**

- Reads a given `launchconfig`
- Fetches needed dependencies on the first run
- Uses the same Ivy cache as sbt itself

So conscript just assumes a convention and helps you adhere to
it.
Suppose you've configured `\$CONSCRIPT_HOME` to `\$HOME/.conscript`.

Then `\$CONSCRIPT_HOME/boot` is used as a boot directory for
all.
App `launchconfig`s are stored according to the Github
project name and the script name, such as:

    \$CONSCRIPT_HOME/foundweekends/conscript/cs/launchconfig

And finally, shell scripts are created in `\$CONSCRIPT_BIN`
(default: `\$CONSCRIPT_HOME/bin`) that reference `launchconfig`s,
for example, `~/.conscript/bin/cs`. This is how the shell script looks like on Mac:

```
#!/bin/sh
java -jar /Users/foo/.conscript/sbt-launch.jar \
  @/Users/foo/.conscript/foundweekends/conscript/cs/launchconfig "\$@"
```

Once the shell script is created, now it's up to the sbt launcher to carry out the actual work of fetching artifacts and running the app.
