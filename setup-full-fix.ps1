# ===========================================================================
# Camping Booking System - Full Setup and Fix Script
# Purpose: Initialize database with UTF-8 encoding and build the project
# ===========================================================================

param(
    [switch]$SkipDatabase = $false,
    [switch]$SkipBackend = $false,
    [switch]$SkipFrontend = $false
)

$ErrorActionPreference = "Inquire"
$OutputEncoding = [Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# Colors
$colors = @{
    Success = "Green"
    Warning = "Yellow"
    Error = "Red"
    Info = "Cyan"
    Section = "Magenta"
}

function Write-Status {
    param([string]$Message, [string]$Type = "Info")
    $color = $colors[$Type]
    Write-Host "[*] $Message" -ForegroundColor $color
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "======================================================================" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "======================================================================" -ForegroundColor Cyan
    Write-Host ""
}

# Function to test command availability
function Test-Command {
    param([string]$Command)
    try {
        $null = Get-Command $Command -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

# Start
Write-Section "Camping Booking System - Setup"

# Get script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Check PostgreSQL
Write-Status "Checking PostgreSQL installation..."
if (-not (Test-Command "psql")) {
    Write-Status "PostgreSQL not found. Please install PostgreSQL first." "Error"
    exit 1
}
Write-Status "PostgreSQL found" "Success"

$DB_HOST     = Read-Host "localhost [localhost]"
if ([string]::IsNullOrWhiteSpace($DB_HOST)) { $DB_HOST = "localhost" }

$DB_PORT     = Read-Host "port [5432]"
if ([string]::IsNullOrWhiteSpace($DB_PORT)) { $DB_PORT = "5432" }

$DB_NAME     = "camping_db"
echo database:camping_db

$DB_USER     = Read-Host "user [postgres]"
if ([string]::IsNullOrWhiteSpace($DB_USER)) { $DB_USER = "postgres" }

$DB_PASSWORD = Read-Host "password [postgres]" -AsSecureString
if ([string]::IsNullOrWhiteSpace($DB_PASSWORD)) {
    $DB_PASSWORD = ConvertTo-SecureString "postgres" -AsPlainText -Force
}
$DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
                [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD))

# Initialize Database
if (-not $SkipDatabase) {
    Write-Section "Step 1: Initialize Database with UTF-8 Encoding"
    
    Write-Status "Creating camping_db database..."
    
    $env:PGPASSWORD = $DB_PASSWORD
    
    # Drop existing database if it exists
    Write-Status "Checking for existing database..."
    psql -h $DB_HOST -U $DB_USER -d postgres -c "DROP DATABASE IF EXISTS camping_db;" 2>$null
    
    # Create new database with UTF-8 encoding
    psql -h $DB_HOST -U $DB_USER -d postgres -c "CREATE DATABASE camping_db ENCODING 'UTF8';" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Database created successfully" "Success"
    } else {
        Write-Status "Failed to create database" "Error"
        exit 1
    }
    
    # Execute schema files
    Write-Status "Executing schema_utf8.sql..."
    psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f "sql/schema_utf8.sql" >$null 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Schema created successfully" "Success"
    } else {
        Write-Status "Failed to create schema" "Error"
        exit 1
    }
    
    Write-Status "Executing views_triggers.sql..."
    psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f "sql/views_triggers.sql" >$null 2>&1
    Write-Status "Views and triggers created" "Success"
    
    Write-Status "Executing data.sql..."
    psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f "sql/data.sql" >$null 2>&1
    Write-Status "Sample data loaded" "Success"
    
    Write-Status "Database initialization completed" "Success"
}

# Build Backend
if (-not $SkipBackend) {
    Write-Section "Step 2: Build Backend"
    
    Write-Status "Cleaning previous build..."
    Set-Location "backend"
    mvn clean -q
    
    Write-Status "Building backend with updated dependencies..."
    mvn install -DskipTests -U
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Backend built successfully" "Success"
    } else {
        Write-Status "Backend build failed" "Error"
        exit 1
    }
    
    Set-Location ".."
}

# Build Frontend
if (-not $SkipFrontend) {
    Write-Section "Step 3: Build Frontend"
    
    Set-Location "frontend"
    
    Write-Status "Installing dependencies..."
    npm install
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Dependencies installed successfully" "Success"
    } else {
        Write-Status "Dependency installation failed" "Error"
        exit 1
    }
    
    Write-Status "Building frontend..."
    npm run build
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Frontend built successfully" "Success"
    } else {
        Write-Status "Frontend build failed" "Error"
        exit 1
    }
    
    Set-Location ".."
}

# Summary
Write-Section "Setup Completed Successfully"

Write-Host "All steps completed! The system is ready to run." -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\start.bat" -ForegroundColor White
Write-Host "2. Access frontend at: http://localhost:5173" -ForegroundColor White
Write-Host "3. Access backend at: http://localhost:8080" -ForegroundColor White
Write-Host ""
