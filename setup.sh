#!/bin/sh

echo "
Fetching current launch configuration...
"
CS=$HOME/.conscript
CSCS=$CS/n8han/conscript/cs
CLC=$CSCS/launchconfig
mkdir -p $CSCS
curl https://github.com/n8han/conscript/raw/master/src/main/conscript/cs/launchconfig \
    > $CLC
echo "
[boot]
  directory: $CS/boot" >> $CLC

mkdir -p $HOME/bin

echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $HOME/bin/cs

chmod a+x $HOME/bin/cs

LJ=sbt-launch-0.7.4.jar
if [ ! -f $CS/$LJ ]; then
    echo "
Fetching launcher...
"
    curl "https://simple-build-tool.googlecode.com/files/$LJ" \
        > $CS/$LJ
    ln -sf $CS/$LJ $CS/sbt-launch.jar
fi

echo "
conscript installed to $HOME/bin/cs
"