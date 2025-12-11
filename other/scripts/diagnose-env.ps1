# =================================================================
# Camping Booking System - Environment Diagnostic Script (Windows)
# This script helps troubleshoot environment setup issues
# =================================================================

param(
    [switch]$Verbose = $false
)

$colors = @{
    Success = "Green"
    Warning = "Yellow"
    Error = "Red"
    Info = "Cyan"
}

function Write-Status {
    param(
        [string]$Message,
        [string]$Type = "Info"
    )
    $color = $colors[$Type]
    Write-Host "[*] $Message" -ForegroundColor $color
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "===================================================================" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "===================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Test-Tool {
    param([string]$ToolName)
    
    try {
        $null = Get-Command $ToolName -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Get-ToolVersion {
    param([string]$ToolName)
    
    try {
        $output = & $ToolName --version 2>&1
        return $output[0]
    } catch {
        return "Error retrieving version"
    }
}

function Get-ToolPath {
    param([string]$ToolName)
    
    try {
        $cmd = Get-Command $ToolName -ErrorAction Stop
        return $cmd.Path
    } catch {
        return "Not found"
    }
}

function Test-Port {
    param(
        [string]$Host = "localhost",
        [int]$Port
    )
    
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $client.Connect($Host, $Port)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

function Check-EnvironmentVariable {
    param([string]$VarName)
    
    $value = [Environment]::GetEnvironmentVariable($VarName, "Machine")
    if ($value) {
        return $value
    } else {
        return "Not set"
    }
}

function Check-ProcessRunning {
    param([string]$ProcessName)
    
    $process = Get-Process -Name $ProcessName -ErrorAction SilentlyContinue
    if ($process) {
        return "Running (PID: $($process.Id))"
    } else {
        return "Not running"
    }
}

# ===== Main =====

Write-Section "Camping Booking System - Environment Diagnostic Tool"

Write-Host "This script will check your system configuration." -ForegroundColor Yellow
Write-Host "If you encounter issues, the output will help troubleshoot." -ForegroundColor Yellow
Write-Host ""

# 1. OS Information
Write-Section "System Information"

$osInfo = Get-WmiObject -Class Win32_OperatingSystem
Write-Host "OS: $($osInfo.Caption)" -ForegroundColor Cyan
Write-Host "Version: $($osInfo.Version)" -ForegroundColor Cyan
Write-Host "Architecture: $([System.Environment]::Is64BitOperatingSystem ? '64-bit' : '32-bit')" -ForegroundColor Cyan
Write-Host ""

# 2. Tools Check
Write-Section "Required Tools Check"

$tools = @(
    @{Name = "java"; Command = "java"}
    @{Name = "javac"; Command = "javac"}
    @{Name = "Maven"; Command = "mvn"}
    @{Name = "PostgreSQL"; Command = "psql"}
    @{Name = "Node.js"; Command = "node"}
    @{Name = "npm"; Command = "npm"}
    @{Name = "Git"; Command = "git"}
)

foreach ($tool in $tools) {
    $status = Test-Tool $tool.Command
    $type = $status ? "Success" : "Error"
    $symbol = $status ? "[✓]" : "[✗]"
    
    Write-Host "$symbol $($tool.Name):" -ForegroundColor ($status ? "Green" : "Red") -NoNewline
    
    if ($status) {
        $version = Get-ToolVersion $tool.Command
        $path = Get-ToolPath $tool.Command
        Write-Host " $version" -ForegroundColor Green
        Write-Host "   Path: $path" -ForegroundColor Gray
    } else {
        Write-Host " Not installed" -ForegroundColor Red
    }
}

Write-Host ""

# 3. Environment Variables
Write-Section "Environment Variables"

$envVars = @(
    "JAVA_HOME",
    "MAVEN_HOME",
    "POSTGRESQL_HOME",
    "PATH"
)

foreach ($var in $envVars) {
    $value = Check-EnvironmentVariable $var
    if ($var -eq "PATH") {
        $pathCount = ($value -split ";").Count
        Write-Host "$var : (contains $pathCount entries)" -ForegroundColor Cyan
        if ($Verbose) {
            ($value -split ";") | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        }
    } else {
        $status = $value -eq "Not set" ? "Error" : "Success"
        $color = $status -eq "Success" ? "Green" : "Yellow"
        Write-Host "$var : $value" -ForegroundColor $color
    }
}

Write-Host ""

# 4. Services Check
Write-Section "Service Status"

$services = @(
    @{Name = "PostgreSQL Server"; Process = "postgres"}
    @{Name = "Spring Boot Backend"; Process = "java"}
)

foreach ($service in $services) {
    $status = Check-ProcessRunning $service.Process
    Write-Host "$($service.Name): $status" -ForegroundColor Cyan
}

Write-Host ""

# 5. Port Availability
Write-Section "Port Availability"

$ports = @(
    @{Name = "PostgreSQL"; Port = 5432}
    @{Name = "Spring Boot"; Port = 8080}
    @{Name = "Frontend Dev"; Port = 5173}
)

foreach ($port in $ports) {
    $available = Test-Port -Port $port.Port
    $status = $available ? "In use" : "Available"
    $color = $available ? "Yellow" : "Green"
    Write-Host "$($port.Name) (Port $($port.Port)): $status" -ForegroundColor $color
}

Write-Host ""

# 6. Database Connection
Write-Section "Database Connection Test"

if (Test-Tool "psql") {
    Write-Host "Attempting to connect to PostgreSQL..." -ForegroundColor Yellow
    
    try {
        $env:PGPASSWORD = "postgres"
        $result = psql -h localhost -U postgres -c "SELECT version();" 2>&1
        
        if ($result) {
            Write-Host "Connection successful!" -ForegroundColor Green
            Write-Host "Database version: $(($result | Select-Object -Last 1).Trim())" -ForegroundColor Green
        }
    } catch {
        Write-Host "Connection failed: $_" -ForegroundColor Red
        Write-Host "Make sure PostgreSQL is running on localhost:5432" -ForegroundColor Yellow
    }
} else {
    Write-Host "PostgreSQL not found. Cannot test connection." -ForegroundColor Red
}

Write-Host ""

# 7. File System Check
Write-Section "Project Structure"

$projectFiles = @(
    "backend\src\main\java\com\camping\",
    "frontend\src\",
    "sql\schema.sql",
    "sql\views_triggers.sql",
    "sql\data.sql",
    "ENVIRONMENT_SETUP.md",
    "start.bat"
)

foreach ($file in $projectFiles) {
    $exists = Test-Path $file
    $status = $exists ? "Found" : "Missing"
    $color = $exists ? "Green" : "Red"
    Write-Host "$file : $status" -ForegroundColor $color
}

Write-Host ""

# 8. Recommendations
Write-Section "Troubleshooting Recommendations"

$hasSoftwareIssues = $false

if (-not (Test-Tool "java")) {
    Write-Host "• Install JDK 11: Download from https://adoptium.net/" -ForegroundColor Yellow
    $hasSoftwareIssues = $true
}

if (-not (Test-Tool "mvn")) {
    Write-Host "• Install Maven: Download from https://maven.apache.org/" -ForegroundColor Yellow
    Write-Host "  Or run: choco install maven" -ForegroundColor Yellow
    $hasSoftwareIssues = $true
}

if (-not (Test-Tool "psql")) {
    Write-Host "• Install PostgreSQL: Download from https://www.postgresql.org/" -ForegroundColor Yellow
    Write-Host "  Remember the password you set for 'postgres' user!" -ForegroundColor Yellow
    $hasSoftwareIssues = $true
}

if (-not (Test-Tool "node")) {
    Write-Host "• Install Node.js: Download from https://nodejs.org/" -ForegroundColor Yellow
    $hasSoftwareIssues = $true
}

if (-not $hasSoftwareIssues) {
    Write-Host "✓ All required tools are installed!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Common issues:" -ForegroundColor Cyan
Write-Host "1. 'Maven not found': Restart your terminal/PowerShell after installation" -ForegroundColor White
Write-Host "2. 'PostgreSQL connection failed': Check if PostgreSQL service is running" -ForegroundColor White
Write-Host "3. 'Port already in use': Kill process using lsof -i :PORT (Linux/Mac) or netstat -ano (Windows)" -ForegroundColor White
Write-Host "4. 'npm install fails': Try: npm cache clean --force" -ForegroundColor White
Write-Host ""

Write-Host "For more help, see: ENVIRONMENT_SETUP.md" -ForegroundColor Cyan
Write-Host ""

Write-Host "Press any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
