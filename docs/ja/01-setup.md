---
out: setup.html
---

  [runnable]: https://dl.bintray.com/foundweekends/maven-releases/org/foundweekends/conscript/conscript_2.11/$version$/conscript_2.11-$version$-proguard.jar
  [ps]: https://raw.githubusercontent.com/foundweekends/conscript/master/setup.ps1

Conscript のインストール方法
--------------------------

### 環境変数の設定

OS にもよって書式は異なるかもしれないが、以下の 3つの環境変数を設定する必要がある。

    export CONSCRIPT_HOME="\$HOME/.conscript"
    export CONSCRIPT_OPTS="-XX:MaxPermSize=512M -Dfile.encoding=UTF-8"
    export PATH=\$CONSCRIPT_HOME/bin:\$PATH

- `CONSCRIPT_HOME` は conscript が様々なファイルをダウンロードする場所だ。
- `CONSCRIPT_OPTS` は conscript を使ってインストールしたアプリに渡される JVM 引数。
- デフォルトでは conscript は起動スクリプトを `\$CONSCRIPT_HOME/bin` 以下に作成する。`\$CONSCRIPT_BIN` を使うことで、その場所を例えば `~/bin/` に変更できる。
- `PATH` は OS のパス変数だ。ここに `\$CONSCRIPT_HOME/bin` もしくは　`\$CONSCRIPT_BIN` を追加する必要がある。

実際のインストールには 3通りの方法がある。

### クロス・プラットフォーム

conscript の[実行可能 jar][runnable] をダウンロードする。最近の OS だとダブルクリックするだけで実行できるけども、それがうまくいかなければ、コマンドラインからも実行できる。

```
\$ java -jar conscript_2.11-$version$-proguard.jar
```

インストール中は「スプラッシュ・スクリーン」が表示される。`cs` がインストールされたというメッセージが書かれるか、エラーメッセージが表示されるまでは閉じてはいけない。

### Linux、 Mac

好みによって、シェルスクリプトをパイプすることによってインストールすることもできる。

```
wget https://raw.githubusercontent.com/foundweekends/conscript/master/setup.sh -O - | sh
```

### Windows

好みによって、この PowerShell [スクリプト][ps]をダウンロードして実行することもできる。
