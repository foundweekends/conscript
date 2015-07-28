#!/bin/sh

read -p "Type configuration directory (e.g. ~/.conscript): " CS
read -p "Type installation directory (e.g.: ~/.bin): " BIN

CSCS=$CS/n8han/conscript/cs
CLC=$CSCS/launchconfig

mkdir -p $CSCS
mkdir -p $BIN

echo "Fetching current launch configuration..."
wget https://raw.githubusercontent.com/n8han/conscript/master/src/main/conscript/cs/launchconfig -O $CLC

echo "
[boot]
  directory: $CS/boot" >> $CLC

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $BIN/cs

chmod a+x $BIN/cs

LJV=0.13.8
LJ="sbt-launch-$LJV.jar"

# If launcher is not in configuration directory
if [ ! -f "$CS/$LJ" ]; then
    echo "Fetching launcher..."
    wget "https://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$LJV/sbt-launch.jar" -O "$CS/$LJ"
    ln -sf $CS/$LJ $CS/sbt-launch.jar
fi

echo "
conscript installed to $BIN/cs
"
