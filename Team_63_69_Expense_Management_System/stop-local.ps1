$ErrorActionPreference = 'Stop'
$bin = Join-Path $PSScriptRoot '.local-run\apache-tomcat-9.0.122\bin'
if (-not (Test-Path -LiteralPath (Join-Path $bin 'shutdown.bat'))) { throw 'Local Tomcat files are missing.' }
$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) { throw 'Java is not on PATH.' }
$oldJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = Split-Path (Split-Path $java.Source -Parent) -Parent
    Push-Location $bin
    & .\catalina.bat stop | Out-Null
} finally {
    Pop-Location
    $env:JAVA_HOME = $oldJavaHome
}
Write-Output 'Tomcat shutdown was requested.'
