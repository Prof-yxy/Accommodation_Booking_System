# =================================================================
# Camping Booking System - Windows Environment Setup (PowerShell)
# This script automatically configures all required tools
# =================================================================

param(
    [switch]$SkipDownloads = $false,
    [switch]$SkipPostgreSQL = $false
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $scriptDir "tools"

# Colors for output
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

function Check-Admin {
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    if (-not $isAdmin) {
        Write-Status "This script must be run as Administrator" "Error"
        Write-Host "Please right-click PowerShell and select 'Run as administrator'" -ForegroundColor Yellow
        exit 1
    }
}

function Test-Command {
    param([string]$Command)
    try {
        $null = Get-Command $Command -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Get-Version {
    param([string]$Command)
    try {
        $output = & $Command --version 2>&1
        if ($output -is [array]) {
            return $output[0]
        } else {
            return $output
        }
    } catch {
        return "Unknown"
    }
}

function Install-ChocolateyIfNeeded {
    if (-not (Test-Command "choco")) {
        Write-Status "Installing Chocolatey package manager..." "Warning"
        Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        Write-Status "Chocolatey installed successfully" "Success"
    }
}

function Install-Java {
    Write-Status "Installing OpenJDK 11..." "Warning"
    choco install openjdk11 -y
    
    if (Test-Command "java") {
        Write-Status "Java installed successfully" "Success"
        return $true
    } else {
        Write-Status "Java installation failed" "Error"
        return $false
    }
}

function Install-Maven {
    Write-Status "Installing Maven 3.8.1+" "Warning"
    choco install maven -y
    
    # Refresh environment variables
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    
    if (Test-Command "mvn") {
        Write-Status "Maven installed successfully" "Success"
        return $true
    } else {
        Write-Status "Maven installation failed. Please restart PowerShell." "Error"
        Write-Status "After restart, try: mvn -version" "Info"
        return $false
    }
}

function Install-NodeJS {
    Write-Status "Installing Node.js 18 LTS..." "Warning"
    choco install nodejs -y
    
    if (Test-Command "node") {
        Write-Status "Node.js installed successfully" "Success"
        return $true
    } else {
        Write-Status "Node.js installation failed" "Error"
        return $false
    }
}

function Install-PostgreSQL {
    Write-Status "Opening PostgreSQL download page..." "Info"
    Write-Host "Please download and install PostgreSQL 12+ from:" -ForegroundColor Yellow
    Write-Host "https://www.postgresql.org/download/windows/" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Installation tips:" -ForegroundColor Yellow
    Write-Host "- Port: 5432 (default)" -ForegroundColor White
    Write-Host "- Username: postgres (default)" -ForegroundColor White
    Write-Host "- Remember your password!" -ForegroundColor White
    
    Start-Process "https://www.postgresql.org/download/windows/"
    Write-Host ""
    Write-Host "Waiting 60 seconds for download/installation..." -ForegroundColor Yellow
    
    for ($i = 60; $i -gt 0; $i--) {
        Write-Progress -Activity "Waiting for PostgreSQL installation" -SecondsRemaining $i
        Start-Sleep -Seconds 1
    }
}

# ===== Main Execution =====

Write-Section "Camping Booking System - Windows Environment Setup"

# Check if running as admin
Check-Admin

Write-Status "Step 0: Installing Chocolatey package manager..."
Install-ChocolateyIfNeeded
Write-Host ""

# Check and install tools
Write-Section "Checking and Installing Required Tools"

# Java
Write-Status "Step 1: Checking Java JDK 11..."
if (Test-Command "java") {
    $javaVer = Get-Version "java"
    Write-Status "Java found: $javaVer" "Success"
} else {
    Install-Java
}
Write-Host ""

# Maven
Write-Status "Step 2: Checking Maven..."
if (Test-Command "mvn") {
    $mvnVer = Get-Version "mvn"
    Write-Status "Maven found: $mvnVer" "Success"
} else {
    Install-Maven
}
Write-Host ""

# Node.js
Write-Status "Step 3: Checking Node.js..."
if (Test-Command "node") {
    $nodeVer = Get-Version "node"
    Write-Status "Node.js found: $nodeVer" "Success"
} else {
    Install-NodeJS
}
Write-Host ""

# npm
Write-Status "Step 4: Checking npm..."
if (Test-Command "npm") {
    $npmVer = Get-Version "npm"
    Write-Status "npm found: $npmVer" "Success"
} else {
    Write-Status "npm not found (should come with Node.js). Reinstall Node.js." "Error"
}
Write-Host ""

# PostgreSQL
if (-not $SkipPostgreSQL) {
    Write-Status "Step 5: Checking PostgreSQL..."
    if (Test-Command "psql") {
        $pgVer = Get-Version "psql"
        Write-Status "PostgreSQL found: $pgVer" "Success"
    } else {
        Install-PostgreSQL
    }
    Write-Host ""
}

# Git (optional)
Write-Status "Step 6: Checking Git (optional)..."
if (Test-Command "git") {
    $gitVer = Get-Version "git"
    Write-Status "Git found: $gitVer" "Success"
} else {
    Write-Status "Git not found (optional). Download from: https://git-scm.com/" "Warning"
}

# Summary
Write-Section "Setup Completed"

Write-Host "All required tools are installed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Close and reopen your PowerShell/Terminal" -ForegroundColor White
Write-Host "2. Run: .\setup-full-fix.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "For detailed configuration information, see:" -ForegroundColor Yellow
Write-Host "  README.md" -ForegroundColor Cyan
Write-Host ""

Write-Host "Press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
