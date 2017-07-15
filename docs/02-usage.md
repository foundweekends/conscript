---
out: usage.html
---

Using conscript
---------------

The main thing you do with conscript is install and update commands based on `launchconfig`.
`launchconfig` files are stored in GitHub projects, which you pass into the `cs` command.
For example, this installs the giter8 templating system:

```
\$ cs foundweekends/giter8
```

`launchconfig`s specify a version of the app to use, but you can override that by specifying an explicit version with another slash:

```
\$ cs foundweekends/giter8/0.2.1
```

Project owners may also decide to push pre-release or other alternate `launchconfig` to different branches on GitHub. Use can tell conscript read `launchconfig`s from another branch with the `--branch` or `-b` option.

```
\$ cs foundweekends/giter8 --branch staging
```

### Cleaning boot directory

If at some point your conscript boot directory contains stale/suspect artifacts such as snapshot releases, you can clean it:

```
\$ cs --clean-boot
```

When you next run any conscripted app (such as `cs`) it will fetch its cleaned dependencies back into the shared boot directory; generally it only has to look as far as the local ivy cache to find them.
