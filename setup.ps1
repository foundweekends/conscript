$wc = New-Object System.Net.WebClient

$proxy_address="$env:http_proxy"

if ($proxy_address) {
  $wc.Proxy = new-object System.Net.WebProxy
  $wc.Proxy.Address = new-object System.URI("$proxy_address")
  $wc.Proxy.useDefaultCredentials = $true
}

echo "
Fetching current launch configuration...
"
$CS="$HOME\.conscript"
$CSCS="$CS\n8han\conscript\cs"
$CLC="$CSCS\launchconfig"
$url="https://raw.githubusercontent.com/n8han/conscript/master/src/main/conscript/cs/launchconfig"
mkdir -Force $CSCS | Out-Null
$wc.DownloadFile($url, $CLC)
echo "
[boot]
  directory: $CS\boot" | Out-File -Append -Encoding "ASCII" $CLC

$BIN="$HOME\bin"
mkdir -Force $BIN | Out-Null

echo "@echo off
java %JAVA_OPTS% -jar $CS\sbt-launch.jar @file:\$CLC %*" | Out-File -Encoding "ASCII" "$BIN\cs.bat"

$LJV="0.13.7"
$LJ="sbt-launch-$LJV.jar"
$url="http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$LJV/sbt-launch.jar"
if (-Not(Test-Path "$CS\$LJ" -PathType Leaf)) {
    echo "
Fetching launcher...
"
    $wc.DownloadFile($url, "$CS\$LJ")
    rm "$CS\sbt-launch.jar" | Out-Null
    cmd /c mklink /h "$CS\sbt-launch.jar" "$CS\$LJ" | Out-Null
}

echo "
conscript installed to $BIN\cs.bat
"

