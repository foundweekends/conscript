#!/bin/sh

echo "
Fetching current launch configuration...
"
CS=$HOME/.conscript
CSCS=$CS/n8han/conscript/cs
CLC=$CSCS/launchconfig
mkdir -p $CSCS
curl https://raw.github.com/n8han/conscript/master/src/main/conscript/cs/launchconfig \
    > $CLC
echo "
[boot]
  directory: $CS/boot" >> $CLC

mkdir -p $HOME/bin

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $HOME/bin/cs

chmod a+x $HOME/bin/cs

LJV=0.10.1
LJ="sbt-launch-$LJV.jar"
if [ ! -f $CS/$LJ ]; then
    echo "
Fetching launcher...
"
    curl "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-tools.sbt/sbt-launch/$LJV/sbt-launch.jar" \
        > $CS/$LJ
    ln -sf $CS/$LJ $CS/sbt-launch.jar
fi

echo "
conscript installed to $HOME/bin/cs
"