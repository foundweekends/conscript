#!/bin/sh

read -p "Type configuration directory (e.g. $HOME/.conscript): " CS
read -p "Type installation directory (e.g. $HOME/.bin): " BIN

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
java -jar $CS/sbt-launch.jar @$CLC \"\$@\"" > "$BIN/cs"

chmod a+x "$BIN/cs"

LJV=1.0.0
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
    echo 'PATH="'"$BIN"':$PATH"' >> $HOME/.bashrc
    exec bash
fi


echo "conscript installed to $BIN/cs"

# Execute Conscript
cs
