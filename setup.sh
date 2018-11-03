#!/bin/sh

## To configure the installation of conscripted application,
## set up the environment variable CONSCRIPT_HOME to something like $HOME/.conscript
## This would the directory where launch JARs and launchconfigs will be donwloaded.
##
## By default, the scripts for the conscripted apps (g8, cs, etc.)
## will be created under CONSCRIPT_HOME/bin.
## This can also be configured using the environment variable CONSCRIPT_BIN.
if [ -z "$CONSCRIPT_HOME" ]
then
  CS_DEFAULT=$HOME/.conscript
  read -p "CONSCRIPT_HOME is not set. Is it ok to use $CS_DEFAULT? (Y/n): " YN
  YN=${YN:-Yes}
  case $YN in
    [Yy]* ) break;;
    * ) exit;;
  esac
  CS="${CS:-$CS_DEFAULT}"
else
  CS="$CONSCRIPT_HOME"
fi
BIN="${CONSCRIPT_BIN:-$CS/bin}"

CSCS="$CS/foundweekends/conscript/cs"
CLC="$CSCS/launchconfig"

mkdir -p $CSCS
mkdir -p "$BIN"

echo "Fetching current launch configuration..."
wget https://raw.githubusercontent.com/foundweekends/conscript/master/src/main/conscript/cs/launchconfig -O $CLC

echo "
[boot]
  directory: $CS/boot" >> "$CLC"

echo "#!/bin/sh
java \$JAVA_OPTS -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > "$BIN/cs"

chmod a+x "$BIN/cs"

LJV=1.0.4
LJ="launcher-$LJV.jar"

# If launcher is not in configuration directory
if [ ! -f "$CS/$LJ" ]; then
    echo "Fetching launcher..."
    wget "https://oss.sonatype.org/content/repositories/public/org/scala-sbt/launcher/$LJV/launcher-$LJV.jar" -O "$CS/$LJ"
    ln -sf "$CS/$LJ" "$CS/sbt-launch.jar"
fi

# Check if BIN is in PATH
bin_in_path=$(echo "$PATH" | grep -i "$BIN")

if [ -z "$bin_in_path" ]
then
    echo 'PATH="$PATH:'"$BIN"'"' >> $HOME/.bashrc
    exec bash
fi


echo "conscript installed to $BIN/cs"

# Execute Conscript
cs
