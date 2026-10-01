# RideLink Microservices - Shutdown Script (PowerShell)
Write-Host "Stopping RideLink Microservices running on ports 8081, 8082, 8083, and 8084..." -ForegroundColor Yellow

@(8081, 8082, 8083, 8084) | ForEach-Object {
    $port = $_
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $pids) {
            Write-Host "Stopping process PID $procId on port $port..." -ForegroundColor Red
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "No process found listening on port $port." -ForegroundColor Gray
    }
}

Write-Host "Done!" -ForegroundColor Green
