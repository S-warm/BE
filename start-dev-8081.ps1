$ErrorActionPreference = "Stop"

Set-Location -LiteralPath $PSScriptRoot

$ports = @(8080, 8081)
$trackedPids = New-Object System.Collections.Generic.HashSet[int]

foreach ($port in $ports) {
  $connections = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
  foreach ($connection in $connections) {
    [void]$trackedPids.Add([int]$connection.OwningProcess)
  }
}

$javaProcesses = @(Get-CimInstance Win32_Process -Filter "name = 'java.exe'" | Where-Object {
  $_.CommandLine -match 'SWARM\\BE|SwarmServerApplication|gradlew bootRun'
})

foreach ($process in $javaProcesses) {
  [void]$trackedPids.Add([int]$process.ProcessId)
}

foreach ($pid in $trackedPids) {
  try {
    $process = Get-Process -Id $pid -ErrorAction Stop
    Write-Host "Stopping existing process: PID=$($process.Id) Name=$($process.ProcessName)"
    Stop-Process -Id $pid -Force -ErrorAction Stop
  } catch {
    Write-Host "Skip PID=$pid ($($_.Exception.Message))"
  }
}

Start-Sleep -Seconds 1

$env:SERVER_PORT = "8081"
Write-Host "Starting BE on port 8081..."
& .\gradlew.bat bootRun
