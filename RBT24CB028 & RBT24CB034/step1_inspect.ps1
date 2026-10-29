$env:PATH = "C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0;$env:PATH"

Write-Host "==================== 1. PORTS 8080 & 4848 ===================="
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -in 8080, 4848 } | Select-Object LocalAddress, LocalPort, OwningProcess, State

Write-Host "`n==================== 2. GLASSFISH DOMAIN STATUS ===================="
$asadmin = "C:\Users\CHINMAY\glassfish7\glassfish\bin\asadmin.bat"
$env:AS_JAVA = "C:\Users\CHINMAY\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2"
& $asadmin list-domains 2>&1

Write-Host "`n==================== 3. RUNNING JAVA / NETBEANS PROCESSES ===================="
Get-Process | Where-Object { $_.ProcessName -match "java|netbeans" } | Select-Object Id, ProcessName, WorkingSet64, Path

Write-Host "`n==================== 4. TARGET DIRECTORY LOCK CHECK ===================="
$testPath = "C:\Users\CHINMAY\OneDrive\Desktop\Mini-Project SDL\target\maven-status\maven-compiler-plugin\compile\default-compile"
if (Test-Path $testPath) {
    Write-Host "File exists: $testPath"
    try {
        $stream = [System.IO.File]::Open($testPath, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
        $stream.Close()
        Write-Host "File is NOT locked by an exclusive write handle right now."
    } catch {
        Write-Host "FILE IS CURRENTLY LOCKED: $($_.Exception.Message)"
    }
} else {
    Write-Host "File does not exist: $testPath"
}
