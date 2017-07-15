---
out: how.html
---

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
例えば、`\$CONSCRIPT_HOME` をデフォルトどおり `\$HOME/.conscript` に設定したとする。

全ての conscript アプリの boot ディレクトリとして `\$CONSCRIPT_HOME/boot` が使用される。
アプリの `launchconfig` は、GitHub プロジェクト名とスクリプトの名前の両方にもとづいたパスに保存される。具体例で説明すると、

    \$CONSCRIPT_HOME/foundweekends/conscript/cs/launchconfig

最後に、その `launchconfig` を参照するシェルスクリプトが、`\$CONSCRIPT_BIN` (デフォルト `\$CONSCRIPT_HOME/bin`) 以下に作成される。
例えば、`~/.conscript/bin/cs` という感じになる。
このシェルスクリプトの内容は、Mac だと以下のようになっている:

```
#!/bin/sh
java -jar /Users/foo/.conscript/sbt-launch.jar \
  @/Users/foo/.conscript/foundweekends/conscript/cs/launchconfig "\$@"
```

この起動スクリプトが作成された後の、アーティファクトを取得したり、アプリを実行したりといった実際の仕事は、sbt ランチャーが行っている。
