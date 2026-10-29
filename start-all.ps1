# SubTrack Startup Script
Write-Host "Starting SubTrack Microservices Ecosystem..." -ForegroundColor Cyan

# Check if MySQL is running
$mysqlCheck = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
if (-not $mysqlCheck.TcpTestSucceeded) {
    Write-Warning "MySQL does not appear to be running on port 3306. Please ensure MySQL is running."
}

# 1. Eureka Server
Write-Host "Starting Eureka Server (Port 8761)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized
Start-Sleep -Seconds 12

# 2. User Service
Write-Host "Starting User Service (Port 8081)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "user-service/target/user-service-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 3. Payment Service
Write-Host "Starting Payment Service (Port 8083)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "payment-service/target/payment-service-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 4. Subscription Service
Write-Host "Starting Subscription Service (Port 8082)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "subscription-service/target/subscription-service-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 5. API Gateway
Write-Host "Starting API Gateway (Port 8080)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 6. Frontend
Write-Host "Starting Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process -FilePath "npm" -ArgumentList "run", "dev" -WorkingDirectory "$PSScriptRoot/frontend" -WindowStyle Minimized

Write-Host "`nAll SubTrack services started successfully!" -ForegroundColor Green
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "- API Gateway:      http://localhost:8080" -ForegroundColor Cyan
Write-Host "- Frontend UI:      http://localhost:5173" -ForegroundColor Cyan
