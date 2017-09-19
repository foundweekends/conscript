# Helper functions
function GetOrElse($Value, $DefaultValue) {
  if (-Not $Value) {
    $DefaultValue
  } else {
    $Value
  }
}

function Get-ProxyAddress() {
  if ($env:JAVA_OPTS -match ".*-Dhttp\.proxyHost=(\S+).*") {
    $proxyHost = $matches[1]
  }
  if ($env:JAVA_OPTS -match ".*-Dhttp\.proxyPort=([0-9]+).*") {
    $proxyPort = $matches[1]
  }

  if ($proxyHost -And $proxyPort) {
    "http://${proxyHost}:${proxyPort}"
  }
}

# Helper object to download artifacts
$wc = New-Object System.Net.WebClient

$proxy_address = Get-ProxyAddress

if ($proxy_address) {
  $wc.Proxy = new-object System.Net.WebProxy
  $wc.Proxy.Address = new-object System.URI("$proxy_address")
  $wc.Proxy.useDefaultCredentials = $true
}

## To configure the installation of conscripted application,
## set up the environment variable CONSCRIPT_HOME to something like $HOME\.conscript
## This would the directory where launch JARs and launchconfigs will be donwloaded.
##
## By default, the scripts for the conscripted apps (g8, cs, etc.)
## will be created under CONSCRIPT_HOME\bin.
## This can also be configured using the environment variable CONSCRIPT_BIN.
if (-Not "$CONSCRIPT_HOME") {
  $CS_DEFAULT = "$HOME\.conscript"
  $YN = Read-Host "CONSCRIPT_HOME is not set. Is it ok to use ${CS_DEFAULT}? (Y/n)"
  if (-Not $YN -Or $YN -match "[Yy].*") {
    $CS = GetOrElse $CS $CS_DEFAULT
  } else {
    exit
  }
} else {
  $CS = "$CONSCRIPT_HOME"
}
$BIN = GetOrElse "$CONSCRIPT_BIN" "$CS\bin"

$CSCS = "$CS\foundweekends\conscript\cs"
$CLC = "$CSCS\launchconfig"

mkdir -Force $CSCS | Out-Null
mkdir -Force $BIN | Out-Null

echo "Fetching current launch configuration..."
$url = "https://raw.githubusercontent.com/foundweekends/conscript/master/src/main/conscript/cs/launchconfig"
$wc.DownloadFile($url, $CLC)

echo "
[boot]
  directory: $CS\boot" | Out-File -Append -Encoding "ASCII" $CLC

echo "@echo off
java %JAVA_OPTS% -jar $CS\sbt-launch.jar @file:\$CLC %*" | Out-File -Encoding "ASCII" "$BIN\cs.bat"

$LJV = "1.0.1"
$LJ = "launcher-$LJV.jar"

# If launcher is not in configuration directory
if (-Not(Test-Path "$CS\$LJ" -PathType Leaf)) {
  echo "Fetching launcher..."
  $url = "https://oss.sonatype.org/content/repositories/public/org/scala-sbt/launcher/$LJV/launcher-$LJV.jar"
  $wc.DownloadFile($url, "$CS/$LJ")
  if (Test-Path "$CS\sbt-launch.jar") {
    rm "$CS\sbt-launch.jar"
  }
  cmd /c mklink /h "$CS\sbt-launch.jar" "$CS\$LJ" | Out-Null
}

# Check if BIN is in PATH
$BIN_IN_PATH = ($env:Path | Select-String -Quiet -SimpleMatch $BIN)
if (-Not $BIN_IN_PATH) {
  $USER_PATH = [System.Environment]::GetEnvironmentVariable("Path", "User")
  [System.Environment]::SetEnvironmentVariable("PATH", $USER_PATH + ";$BIN", "User")
  $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
}

echo "conscript installed to $BIN\cs"

# Execute Conscript
cs
