---
out: private-repos.html
---

[oauth]: http://developer.github.com/v3/oauth/#create-a-new-authorization
[tokens]: https://github.com/settings/applications

プライベート・リポジトリ
--------------------

conscript は GitHub の [非 web アプリ][oauth]用の OAuth フローを使うことで、プライベートな GitHub リポジトリもサポートしている。

認証して、永久トークンを保存するには、`--auth` オプションを使う:

```
\$ cs --auth yourname:yourpass
```

これは、アクセス・トークンを `~/.conscript/config` に保存して、以降全ての
`cs` 実行時に用いられる。このトークンは [GitHub account settings][tokens] よりいつでも取り消すことができる。

sbt ランチャーは、sbt 本体同様にプライベートな Maven もしくは Ivy リポジトリをアクセスすることができる。
`~/.ivy2/.credentials` などの credential プロパティファイルを `sbt.boot.credentials` JVM プロパティもしくは、`SBT_CREDENTIALS` 環境変数にて指定する。
sbt ランチャーは、これらの認証情報を用いて realm
内の保護されたリソースをアクセスすることができる。
