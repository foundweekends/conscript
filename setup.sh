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

BIN=$HOME/bin
mkdir -p $BIN

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $BIN/cs

chmod a+x $BIN/cs

LJV=0.13.0
LJ="sbt-launch-$LJV.jar"
if [ ! -f $CS/$LJ ]; then
    echo "
Fetching launcher...
"
    curl "http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$LJV/sbt-launch.jar" \
        > $CS/$LJ
    ln -sf $CS/$LJ $CS/sbt-launch.jar
fi

echo "
conscript installed to $BIN/cs
"
