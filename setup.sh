#!/bin/sh

read -p "Type configuration directory (e.g. ~/.conscript): " CS
read -p "Type installation directory (e.g.: ~/.bin): " BIN

CSCS=$CS/n8han/conscript/cs
CLC=$CSCS/launchconfig

mkdir -p $CSCS
mkdir -p $BIN

echo "Fetching current launch configuration..."

curl https://raw.githubusercontent.com/n8han/conscript/master/src/main/conscript/cs/launchconfig \
    > $CLC
echo "
[boot]
  directory: $CS/boot" >> $CLC

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $BIN/cs

chmod a+x $BIN/cs

LJV=0.13.7
LJ="sbt-launch-$LJV.jar"
if [ ! -f $CS/$LJ ]; then
    echo "
Fetching launcher...
"
    curl -L "http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$LJV/sbt-launch.jar" \
        > $CS/$LJ
    ln -sf $CS/$LJ $CS/sbt-launch.jar
fi

echo "
conscript installed to $BIN/cs
"
