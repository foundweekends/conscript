#!/bin/sh

echo "
Fetching current launch configuration...
"
CS=$HOME/.conscript
CLC=$CS/n8han/conscript
mkdir -p $HOME/.conscript/n8han
curl "https://github.com/n8han/conscript/raw/master/src/main/conscript/launchconfig"\
    > $CLC
echo "
[boot]
  directory: $CS/boot" >> $CLC

mkdir -p $HOME/bin

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar  @$CLC \"\$@\"" > $HOME/bin/cs

chmod a+x $HOME/bin/cs

echo "
Fetching launcher...
"

curl "https://simple-build-tool.googlecode.com/files/sbt-launch-0.7.4.jar" \
    > $CS/sbt-launch.jar

echo "
conscript installed to $HOME/bin/cs
"