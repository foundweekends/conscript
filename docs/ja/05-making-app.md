---
out: making-app.html
---

  [sbtlauncher]: http://www.scala-sbt.org/0.13/docs/Sbt-Launcher.html
  [scopt]: https://github.com/scopt/scopt

Conscripted app の作り方
-----------------------

conscript を使った独自アプリを作ってみよう。

### ConscriptPlugin

`ConscriptPlugin` を使ってアプリを作る方法をここでは紹介する。
まずは以下を `project/concript.sbt` に書く:

```scala
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "$version$")
```

次に、`build.sbt` でアプリのためのサブプロジェクトで `ConscriptPlugin` を有効にする:

```
lazy val root = (project in file(".")).
  enablePlugins(ConscriptPlugin).
  settings(
    // other settings here
  )
```

このプラグインは sbt の launcher-interface に対して「provided」な依存性を追加する。

### エントリーポイント

`xsbti.AppMain` を実装して、アプリへのエントリーポイントを作る。

```scala
package example

class HelloApp extends xsbti.AppMain {
  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    // get the version of Scala used to launch the application
    val scalaVersion = configuration.provider.scalaProvider.version

    // Print a message and the arguments to the application
    println("Hello world!  Running Scala " + scalaVersion)
    configuration.arguments.foreach(println)

    new Exit(0)
  }
  class Exit(val code: Int) extends xsbti.Exit
}
```

1つか 2つのコマンドライン・オプションを作った後は、多分
[scopt][scopt] みたいなコマンドライン・パーサを使うことをお勧めする。

### launchconfig

次に、`launchconfig` ファイルを `src/main/conscript/XYZ/launchconfig` に置く
(ここで、`XYZ` は `g8` や `cs` といったスクリプト名に置き換える):

```
[app]
  version: 0.1.0
  org: com.example
  name: hello
  class: example.HelloApp
[scala]
  version: 2.11.9
[repositories]
  local
  maven-central
  sonatype-releases: https://oss.sonatype.org/content/repositories/releases/
```

`launchconfig` に関する詳細は sbt レファレンス・マニュアルの　[sbt Launcher][sbtlauncher] の項目を参照。

### Bintray

repositories の項目には、Bintray を使った Maven リポジトリを含む任意のリポジトリを追加できる。
例えば、foundweekends の Bintray リポジトリの設定は以下のようになっている。

```
  foundweekends-maven-releases: https://dl.bintray.com/foundweekends/maven-releases/
```

### csRun

アプリを `csRun XYZ` コマンドを sbt シェルから打ち込むことで、アプリをテストできる。
