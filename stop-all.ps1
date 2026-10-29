# SubTrack Shutdown Script
Write-Host "Stopping all SubTrack services..." -ForegroundColor Cyan

$ports = @(8761, 8080, 8081, 8082, 8083, 5173)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($p in $pids) {
            try {
                Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
                Write-Host "Terminated process $p listening on port $port" -ForegroundColor Green
            } catch {
                Write-Warning "Could not stop process $p on port $port"
            }
        }
    }
}

Write-Host "All SubTrack services stopped." -ForegroundColor Yellow
