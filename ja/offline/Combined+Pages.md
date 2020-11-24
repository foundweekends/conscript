
Conscript
=========

Conscript は、GitHub と Maven リポジトリをインフラとして使った Scala アプリのための配信機構だ。APT や Home Brew のように、アプリのインストールや更新を行うことができる。

多分思ったより少ないことしか行わない。なぜなら sbt ランチャーが思ったより多くのことをこなすからだ。それに関してはまた後で。


  [runnable]: https://dl.bintray.com/foundweekends/maven-releases/org/foundweekends/conscript/conscript_2.11/0.5.3/conscript_2.11-0.5.3-proguard.jar
  [ps]: https://raw.githubusercontent.com/foundweekends/conscript/master/setup.ps1

Conscript のインストール方法
--------------------------

### 環境変数の設定

OS にもよって書式は異なるかもしれないが、以下の 3つの環境変数を設定する必要がある。

    export CONSCRIPT_HOME="$HOME/.conscript"
    export CONSCRIPT_OPTS="-XX:MaxPermSize=512M -Dfile.encoding=UTF-8"
    export PATH=$CONSCRIPT_HOME/bin:$PATH

- `CONSCRIPT_HOME` は conscript が様々なファイルをダウンロードする場所だ。
- `CONSCRIPT_OPTS` は conscript を使ってインストールしたアプリに渡される JVM 引数。
- デフォルトでは conscript は起動スクリプトを `$CONSCRIPT_HOME/bin` 以下に作成する。`$CONSCRIPT_BIN` を使うことで、その場所を例えば `~/bin/` に変更できる。
- `PATH` は OS のパス変数だ。ここに `$CONSCRIPT_HOME/bin` もしくは　`$CONSCRIPT_BIN` を追加する必要がある。

実際のインストールには 3通りの方法がある。

### クロス・プラットフォーム

conscript の[実行可能 jar][runnable] をダウンロードする。最近の OS だとダブルクリックするだけで実行できるけども、それがうまくいかなければ、コマンドラインからも実行できる。

```
$ java -jar conscript_2.11-0.5.3-proguard.jar
```

インストール中は「スプラッシュ・スクリーン」が表示される。`cs` がインストールされたというメッセージが書かれるか、エラーメッセージが表示されるまでは閉じてはいけない。

### Linux、 Mac

好みによって、シェルスクリプトをパイプすることによってインストールすることもできる。

```
wget https://raw.githubusercontent.com/foundweekends/conscript/master/setup.sh -O - | sh
```

### Windows

好みによって、この PowerShell [スクリプト][ps]をダウンロードして実行することもできる。


Conscript の使い方
-----------------

conscript を用いてやる主なことは `launchconfig` に基いて書かれているコマンドのインストールと更新だ。
`launchconfig` ファイルは GitHub プロジェクトにて保存されていて、それを `cs` コマンドから指定する。
例えば、以下のようにして giter8 テンプレートシステムをインストールする:

```
$ cs foundweekends/giter8
```

`launchconfig` はアプリのバージョンも指定するが、もう一つ「/」を付けて別のバージョン番号を指定することも可能だ:

```
$ cs foundweekends/giter8/0.2.1
```

プロジェクトの作者はプレリリースや別版の `launchconfig` を Gibhub 上の別のブランチに push するかもしれない。`--branch` もしくは `-b` オプションを使って、別のブランチから `launchconfig` を読み込むことができる。

```
$ cs foundweekends/giter8 --branch staging
```

### boot ディレクトリのクリーン

スナップショットなどを使っていて、conscript の boot ディレクトリが古かったり、不明なアーティファクトが入っている場合はクリーンすることが可能だ:

```
$ cs --clean-boot
```

これで、次に (`cs` のような) conscript アプリを実行すると、boot ディレクトリに新しくライブラリ依存性を取ってくるようになる。大抵はローカルの Ivy キャッシュに行くだけだと思うけど。


Conscript の仕組み
-----------------

conscript は思ったよりも**少ない**ことしか行わない。なぜなら sbt ランチャーが思ったより多くのことをこなすからだ。何を言っているんだ?

<br>**conscript が行うこと:**

- GitHub プロジェクトに `launchconfig` (ランチャー設定ファイル) があるかを問い合わせる。
- `launchconfig` をローカルのファイルシステムにコピーして、boot path の設定を書き換える。
- ランチャー設定ファイルにもとづいて起動させるためのスクリプトを作成する。

**sbt ランチャーが行うこと:**

- 指定された `launchconfig` を読み込む。
- 初回起動時に必要なライブラリ依存性を取ってくる。
- sbt 本体同様に Ivy cache を用いてキャッシュする。

conscript は、要するに sbt ランチャー周りの慣用を決め打ちして使いやすくしただけにすぎない。
例えば、`$CONSCRIPT_HOME` をデフォルトどおり `$HOME/.conscript` に設定したとする。

全ての conscript アプリの boot ディレクトリとして `$CONSCRIPT_HOME/boot` が使用される。
アプリの `launchconfig` は、GitHub プロジェクト名とスクリプトの名前の両方にもとづいたパスに保存される。具体例で説明すると、

    $CONSCRIPT_HOME/foundweekends/conscript/cs/launchconfig

最後に、その `launchconfig` を参照するシェルスクリプトが、`$CONSCRIPT_BIN` (デフォルト `$CONSCRIPT_HOME/bin`) 以下に作成される。
例えば、`~/.conscript/bin/cs` という感じになる。
このシェルスクリプトの内容は、Mac だと以下のようになっている:

```
#!/bin/sh
java -jar /Users/foo/.conscript/sbt-launch.jar \
  @/Users/foo/.conscript/foundweekends/conscript/cs/launchconfig "$@"
```

この起動スクリプトが作成された後の、アーティファクトを取得したり、アプリを実行したりといった実際の仕事は、sbt ランチャーが行っている。


[oauth]: http://developer.github.com/v3/oauth/#create-a-new-authorization
[tokens]: https://github.com/settings/applications

プライベート・リポジトリ
--------------------

conscript は GitHub の [非 web アプリ][oauth]用の OAuth フローを使うことで、プライベートな GitHub リポジトリもサポートしている。

認証して、永久トークンを保存するには、`--auth` オプションを使う:

```
$ cs --auth yourname:yourpass
```

これは、アクセス・トークンを `~/.conscript/config` に保存して、以降全ての
`cs` 実行時に用いられる。このトークンは [GitHub account settings][tokens] よりいつでも取り消すことができる。

sbt ランチャーは、sbt 本体同様にプライベートな Maven もしくは Ivy リポジトリをアクセスすることができる。
`~/.ivy2/.credentials` などの credential プロパティファイルを `sbt.boot.credentials` JVM プロパティもしくは、`SBT_CREDENTIALS` 環境変数にて指定する。
sbt ランチャーは、これらの認証情報を用いて realm
内の保護されたリソースをアクセスすることができる。


  [sbtlauncher]: http://www.scala-sbt.org/0.13/docs/Sbt-Launcher.html
  [scopt]: https://github.com/scopt/scopt

Conscripted app の作り方
-----------------------

conscript を使った独自アプリを作ってみよう。

### ConscriptPlugin

`ConscriptPlugin` を使ってアプリを作る方法をここでは紹介する。
まずは以下を `project/concript.sbt` に書く:

```scala
addSbtPlugin("org.foundweekends.conscript" % "sbt-conscript" % "0.5.3")
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
  version: 2.11.12
[repositories]
  local
  maven-central
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


Conscripted apps
----------------

Conscript を使っているプロジェクトのリストは[英語版](../conscripted-apps.html)にある。

あなたが Conscript を使っているなら是非プロジェクトを追加して pull request を送ってほしい。
