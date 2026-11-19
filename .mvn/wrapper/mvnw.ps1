# Resolves (downloading on first use) the Maven distribution declared in maven-wrapper.properties
# and prints the path to its mvn.cmd launcher.
$ErrorActionPreference = 'Stop'
$propsFile = Join-Path $PSScriptRoot 'maven-wrapper.properties'
$line = Get-Content -LiteralPath $propsFile | Where-Object { $_ -match '^distributionUrl=' } | Select-Object -First 1
$url = ($line -replace '^distributionUrl=', '').Trim()
$name = [IO.Path]::GetFileNameWithoutExtension($url) -replace '-bin$', ''
$base = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
$mvn = Join-Path (Join-Path $base $name) 'bin\mvn.cmd'

if (-not (Test-Path -LiteralPath $mvn)) {
    New-Item -ItemType Directory -Force -Path $base | Out-Null
    $zip = Join-Path $base "$name.zip"
    [Console]::Error.WriteLine("[mvnw] Downloading $url")
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object Net.WebClient).DownloadFile($url, $zip)
    Expand-Archive -LiteralPath $zip -DestinationPath $base -Force
    Remove-Item -LiteralPath $zip -Force
}
Write-Output $mvn
