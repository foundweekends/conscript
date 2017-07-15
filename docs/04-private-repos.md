---
out: private-repos.html
---

[oauth]: http://developer.github.com/v3/oauth/#create-a-new-authorization
[tokens]: https://github.com/settings/applications

Private repositories
--------------------

Conscript supports **private github repos** using github's oauth flow
[for non-web apps][oauth].

To authenticate and store a permanent token, use the `--auth` parameter:

```
\$ cs --auth yourname:yourpass
```

This stores an access token in `~/.conscript/config` which is used for
all future `cs` invocations. You can revoke tokens at any time in your
[GitHub account settings][tokens].

The sbt launcher can access **private Maven/Ivy repos** just as sbt
itself can. Specify a credentials properties file, such as
`~/.ivy2/.credentials`, in the `sbt.boot.credentials` JVM property or
`SBT_CREDENTIALS` environment variable. The launcher will use these
credentials when accessing protected resources in the specified realm.
