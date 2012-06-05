#!/bin/sh

BIN=$HOME/bin
mkdir -p $BIN

if [ -a $BIN/cs ] ; then
   if ! [ -f $BIN/cs ] ; then
       echo "$BIN/cs exists but is not a regular file.  Not clobbering.  Remove $BIN/cs and run this setup script again."
       exit 1
   elif ! grep -q sbt-launch\\.jar $BIN/cs ; then
       echo "$BIN/cs exists, but it does not not appear to be an old conscript file.  Not clobbering.  Remove $BIN/cs and run this setup script again."
       exit 2
   else
       echo
       echo "Existing $BIN/cs found.  Will overwrite." 
   fi
fi



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



echo "#!/bin/sh
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > $BIN/cs

chmod a+x $BIN/cs

LJV=0.11.3
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
