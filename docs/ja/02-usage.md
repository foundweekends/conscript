---
out: usage.html
---

Conscript の使い方
-----------------

conscript を用いてやる主なことは `launchconfig` に基いて書かれているコマンドのインストールと更新だ。
`launchconfig` ファイルは GitHub プロジェクトにて保存されていて、それを `cs` コマンドから指定する。
例えば、以下のようにして giter8 テンプレートシステムをインストールする:

```
\$ cs foundweekends/giter8
```

`launchconfig` はアプリのバージョンも指定するが、もう一つ「/」を付けて別のバージョン番号を指定することも可能だ:

```
\$ cs foundweekends/giter8/0.2.1
```

プロジェクトの作者はプレリリースや別版の `launchconfig` を Gibhub 上の別のブランチに push するかもしれない。`--branch` もしくは `-b` オプションを使って、別のブランチから `launchconfig` を読み込むことができる。

```
\$ cs foundweekends/giter8 --branch staging
```

### boot ディレクトリのクリーン

スナップショットなどを使っていて、conscript の boot ディレクトリが古かったり、不明なアーティファクトが入っている場合はクリーンすることが可能だ:

```
\$ cs --clean-boot
```

これで、次に (`cs` のような) conscript アプリを実行すると、boot ディレクトリに新しくライブラリ依存性を取ってくるようになる。大抵はローカルの Ivy キャッシュに行くだけだと思うけど。
