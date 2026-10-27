$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$tomcat = Join-Path $project '.local-run\apache-tomcat-9.0.122'
$war = Join-Path $project 'target\expense-management.war'
$bin = Join-Path $tomcat 'bin'

if (-not (Test-Path -LiteralPath $war)) { throw 'Build the project first: mvn clean package' }
if (-not (Test-Path -LiteralPath (Join-Path $bin 'startup.bat'))) { throw 'Local Tomcat files are missing.' }
if (Test-NetConnection -ComputerName 127.0.0.1 -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue) {
    throw 'Port 8080 is already in use. Stop the other server before starting this project.'
}
$deployedApp = Join-Path $tomcat 'webapps\expense-management'
if (Test-Path -LiteralPath $deployedApp) { Remove-Item -LiteralPath $deployedApp -Recurse -Force }

$dbUser = [Environment]::GetEnvironmentVariable('DB_USER', 'Machine')
$dbPassword = [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Machine')
$dbUrl = [Environment]::GetEnvironmentVariable('DB_URL', 'Machine')
$javaCommand = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javaCommand) { $javaCommand = Get-Command java -ErrorAction SilentlyContinue }
if (-not $javaCommand) { throw 'Install a JDK and make sure java.exe or javac.exe is on PATH.' }
$javaHome = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }
if ([string]::IsNullOrWhiteSpace($dbPassword)) { throw 'Set DB_PASSWORD in Windows Environment Variables first.' }
if ([string]::IsNullOrWhiteSpace($dbUrl)) { $dbUrl = 'jdbc:mysql://localhost:3306/expense_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' }

$oldUser = $env:DB_USER; $oldPassword = $env:DB_PASSWORD; $oldUrl = $env:DB_URL; $oldJavaHome = $env:JAVA_HOME
try {
    $env:DB_USER = $dbUser
    $env:DB_PASSWORD = $dbPassword
    $env:DB_URL = $dbUrl
    $env:JAVA_HOME = $javaHome
    Copy-Item -LiteralPath $war -Destination (Join-Path $tomcat 'webapps\expense-management.war') -Force
    Start-Process -FilePath (Join-Path $bin 'startup.bat') -WorkingDirectory $bin -WindowStyle Hidden | Out-Null
} finally {
    $env:DB_USER = $oldUser; $env:DB_PASSWORD = $oldPassword; $env:DB_URL = $oldUrl; $env:JAVA_HOME = $oldJavaHome
    $dbPassword = $null
}

$ready = $false
for ($i = 0; $i -lt 45; $i++) {
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-WebRequest -Uri 'http://localhost:8080/expense-management/expenses' -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
}
if (-not $ready) { throw 'Tomcat started, but the app did not return successfully. Review .local-run\apache-tomcat-9.0.122\logs for details.' }
Write-Output 'Expense Management is running at http://localhost:8080/expense-management/'
Start-Process 'http://localhost:8080/expense-management/'
