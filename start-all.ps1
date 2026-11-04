# SubTrack Startup Script
Write-Host "Starting SubTrack Microservices Ecosystem..." -ForegroundColor Cyan

# 0. Load .env file if present
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Write-Host "Loading environment variables from .env..." -ForegroundColor DarkGray
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $varName = $parts[0].Trim()
            $varVal = $parts[1].Trim().Trim('"').Trim("'")
            if (-not [string]::IsNullOrEmpty($varName)) {
                [System.Environment]::SetEnvironmentVariable($varName, $varVal, "Process")
                Set-Item "env:$varName" $varVal
            }
        }
    }
}

# 0b. Validate required security environment variables (fail-fast before starting services)
$requiredVars = @("JWT_SECRET", "INTERNAL_SERVICE_SECRET", "INTERNAL_GATEWAY_TOKEN")
$missingOrInvalid = @()

foreach ($var in $requiredVars) {
    $val = [System.Environment]::GetEnvironmentVariable($var)
    if ([string]::IsNullOrWhiteSpace($val)) {
        $missingOrInvalid += "$var (missing or empty)"
    } elseif ($val.Length -lt 32) {
        $missingOrInvalid += "$var (too short: must be at least 32 characters / 256 bits)"
    }
}

if ($missingOrInvalid.Count -gt 0) {
    Write-Host "`n[FATAL CONFIGURATION ERROR] Required security environment variables are not properly configured:" -ForegroundColor Red
    foreach ($err in $missingOrInvalid) {
        Write-Host " - $err" -ForegroundColor Yellow
    }
    Write-Host "`nAction Required:" -ForegroundColor Cyan
    Write-Host " 1. Copy .env.example to .env in the project root." -ForegroundColor White
    Write-Host " 2. Populate JWT_SECRET, INTERNAL_SERVICE_SECRET, and INTERNAL_GATEWAY_TOKEN with high-entropy values (>= 32 chars)." -ForegroundColor White
    Write-Host " 3. Rerun start-all.ps1." -ForegroundColor White
    exit 1
}

# Check if MySQL is running
$mysqlCheck = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue
if (-not $mysqlCheck.TcpTestSucceeded) {
    Write-Warning "MySQL does not appear to be running on port 3306. Please ensure MySQL is running."
}

# Extract active configuration values for process invocation
$jwtSecret = [System.Environment]::GetEnvironmentVariable("JWT_SECRET")
$internalSecret = [System.Environment]::GetEnvironmentVariable("INTERNAL_SERVICE_SECRET")
$gatewayToken = [System.Environment]::GetEnvironmentVariable("INTERNAL_GATEWAY_TOKEN")
$dbUser = [System.Environment]::GetEnvironmentVariable("DB_USERNAME")
if ([string]::IsNullOrEmpty($dbUser)) { $dbUser = "root" }
$dbPass = [System.Environment]::GetEnvironmentVariable("DB_PASSWORD")
if ([string]::IsNullOrEmpty($dbPass)) { $dbPass = "Siva1234" }
$mailEnabled = [System.Environment]::GetEnvironmentVariable("MAIL_ENABLED")
if ([string]::IsNullOrEmpty($mailEnabled)) { $mailEnabled = "true" }
$smtpHost = [System.Environment]::GetEnvironmentVariable("SMTP_HOST")
if ([string]::IsNullOrEmpty($smtpHost)) { $smtpHost = "smtp.gmail.com" }
$smtpPort = [System.Environment]::GetEnvironmentVariable("SMTP_PORT")
if ([string]::IsNullOrEmpty($smtpPort)) { $smtpPort = "587" }
$smtpUser = [System.Environment]::GetEnvironmentVariable("SMTP_USERNAME")
$smtpPass = [System.Environment]::GetEnvironmentVariable("SMTP_PASSWORD")

# 1. Eureka Server
Write-Host "Starting Eureka Server (Port 8761)..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList "-jar", "eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar" -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized
Start-Sleep -Seconds 12

# 2. User Service
Write-Host "Starting User Service (Port 8081)..." -ForegroundColor Yellow
$userArgs = @("-DJWT_SECRET=$jwtSecret", "-DINTERNAL_SERVICE_SECRET=$internalSecret", "-DINTERNAL_GATEWAY_TOKEN=$gatewayToken", "-DDB_USERNAME=$dbUser", "-DDB_PASSWORD=$dbPass", "-jar", "user-service/target/user-service-0.0.1-SNAPSHOT.jar")
Start-Process -FilePath "java" -ArgumentList $userArgs -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 3. Payment Service
Write-Host "Starting Payment Service (Port 8083)..." -ForegroundColor Yellow
$paymentArgs = @("-DINTERNAL_SERVICE_SECRET=$internalSecret", "-DINTERNAL_GATEWAY_TOKEN=$gatewayToken", "-DDB_USERNAME=$dbUser", "-DDB_PASSWORD=$dbPass", "-jar", "payment-service/target/payment-service-0.0.1-SNAPSHOT.jar")
Start-Process -FilePath "java" -ArgumentList $paymentArgs -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 4. Subscription Service
Write-Host "Starting Subscription Service (Port 8082)..." -ForegroundColor Yellow
$subArgs = @("-DINTERNAL_SERVICE_SECRET=$internalSecret", "-DINTERNAL_GATEWAY_TOKEN=$gatewayToken", "-DDB_USERNAME=$dbUser", "-DDB_PASSWORD=$dbPass", "-DMAIL_ENABLED=$mailEnabled", "-Dspring.mail.enabled=$mailEnabled", "-DSMTP_HOST=$smtpHost", "-DSMTP_PORT=$smtpPort", "-DSMTP_USERNAME=$smtpUser", "-DSMTP_PASSWORD=$smtpPass", "-jar", "subscription-service/target/subscription-service-0.0.1-SNAPSHOT.jar")
Start-Process -FilePath "java" -ArgumentList $subArgs -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 5. API Gateway
Write-Host "Starting API Gateway (Port 8080)..." -ForegroundColor Yellow
$gatewayArgs = @("-DJWT_SECRET=$jwtSecret", "-DINTERNAL_SERVICE_SECRET=$internalSecret", "-DINTERNAL_GATEWAY_TOKEN=$gatewayToken", "-jar", "api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar")
Start-Process -FilePath "java" -ArgumentList $gatewayArgs -WorkingDirectory "$PSScriptRoot" -WindowStyle Minimized

# 6. Frontend
Write-Host "Starting Frontend (Port 5173)..." -ForegroundColor Yellow
Start-Process -FilePath "npm" -ArgumentList "run", "dev" -WorkingDirectory "$PSScriptRoot/frontend" -WindowStyle Minimized

Write-Host "`nAll SubTrack services started successfully!" -ForegroundColor Green
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "- API Gateway:      http://localhost:8080" -ForegroundColor Cyan
Write-Host "- Frontend UI:      http://localhost:5173" -ForegroundColor Cyan
