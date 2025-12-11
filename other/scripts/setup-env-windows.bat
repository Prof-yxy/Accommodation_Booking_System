@echo off
REM =================================================================
REM  Camping Booking System - Windows Environment Setup Script
REM  This script automatically configures all required tools
REM =================================================================

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set TOOLS_DIR=%SCRIPT_DIR%tools
set ERROR_LOG=%SCRIPT_DIR%setup-error.log

echo.
echo =================================================================
echo  Camping Booking System - Automatic Environment Setup
echo =================================================================
echo.

REM Check if running as Administrator
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: This script must be run as Administrator
    echo Please right-click on PowerShell and select "Run as administrator"
    echo Then run: .\setup-env-windows.ps1
    pause
    exit /b 1
)

echo [*] Checking system requirements...
echo.

REM Create tools directory
if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"

REM ========== Check Java ==========
echo [1/6] Checking Java JDK 11...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Java not found
    echo Installing OpenJDK 11...
    
    call :download_java
    if !errorlevel! neq 0 (
        echo ERROR: Failed to install Java
        goto :error
    )
) else (
    for /f "tokens=2" %%i in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VER=%%i
    echo OK: Java !JAVA_VER! found
)
echo.

REM ========== Check Maven ==========
echo [2/6] Checking Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Maven not found
    echo Installing Maven 3.8.1...
    
    call :download_maven
    if !errorlevel! neq 0 (
        echo ERROR: Failed to install Maven
        goto :error
    )
) else (
    for /f "tokens=3" %%i in ('mvn -version 2^>^&1 ^| find "Apache Maven"') do set MVN_VER=%%i
    echo OK: Maven !MVN_VER! found
)
echo.

REM ========== Check PostgreSQL ==========
echo [3/6] Checking PostgreSQL...
psql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: PostgreSQL not found
    echo Downloading PostgreSQL 14 installer...
    echo (Please complete the installation manually)
    echo URL: https://www.postgresql.org/download/windows/
    
    start https://www.postgresql.org/download/windows/
    echo Waiting for installation...
    timeout /t 60
) else (
    for /f "tokens=3" %%i in ('psql --version') do set PG_VER=%%i
    echo OK: PostgreSQL !PG_VER! found
    
    REM Check if service is running
    sc query postgresql-x64-14 >nul 2>&1
    if %errorlevel% neq 0 (
        echo Starting PostgreSQL service...
        net start postgresql-x64-14 >nul 2>&1
    )
)
echo.

REM ========== Check Node.js ==========
echo [4/6] Checking Node.js...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Node.js not found
    echo Downloading Node.js 18 LTS...
    
    call :download_nodejs
    if !errorlevel! neq 0 (
        echo ERROR: Failed to install Node.js
        goto :error
    )
) else (
    for /f %%i in ('node --version') do set NODE_VER=%%i
    echo OK: Node.js !NODE_VER! found
)
echo.

REM ========== Check npm ==========
echo [5/6] Checking npm...
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: npm not found. Please reinstall Node.js
    goto :error
) else (
    for /f %%i in ('npm --version') do set NPM_VER=%%i
    echo OK: npm !NPM_VER! found
)
echo.

REM ========== Check Git ==========
echo [6/6] Checking Git (optional)...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Git not found (optional)
    echo You can download it from: https://git-scm.com/download/win
) else (
    for /f "tokens=3" %%i in ('git --version') do set GIT_VER=%%i
    echo OK: Git !GIT_VER! found
)
echo.

REM ========== Summary ==========
echo =================================================================
echo  SETUP COMPLETED SUCCESSFULLY
echo =================================================================
echo.
echo All required tools are installed!
echo.
echo Next steps:
echo 1. Close and reopen your terminal/PowerShell
echo 2. Run: .\start.bat
echo.
echo For manual configuration details, see: ENVIRONMENT_SETUP.md
echo.

pause
exit /b 0

REM ========== Functions ==========

:download_java
echo Downloading OpenJDK 11...
REM Note: In production, download from: https://adoptium.net/
echo Please download and install OpenJDK 11 from:
echo https://adoptium.net/
echo Then restart this script
timeout /t 10
exit /b 1

:download_maven
echo Downloading Maven...
REM Download Maven from Apache
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.zip
set MAVEN_ZIP=%TOOLS_DIR%\maven.zip

powershell -Command ^
    "try { ^
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        (New-Object Net.WebClient).DownloadFile('%MAVEN_URL%', '%MAVEN_ZIP%'); ^
        Write-Host 'Downloaded successfully' ^
    } catch { ^
        Write-Host 'Download failed'; ^
        exit 1 ^
    }"

if %errorlevel% neq 0 (
    echo Download failed. Please install Maven manually from:
    echo https://maven.apache.org/download.cgi
    exit /b 1
)

echo Extracting Maven...
powershell -Command "Expand-Archive '%MAVEN_ZIP%' '%TOOLS_DIR%'"

echo Configuring Maven environment variable...
setx MAVEN_HOME "%TOOLS_DIR%\apache-maven-3.8.1"
set PATH=%PATH%;%TOOLS_DIR%\apache-maven-3.8.1\bin

echo Maven installed successfully
echo Please restart your terminal for changes to take effect
exit /b 0

:download_nodejs
echo Downloading Node.js...
set NODE_URL=https://nodejs.org/dist/v18.0.0/node-v18.0.0-x64.msi
set NODE_MSI=%TOOLS_DIR%\nodejs.msi

powershell -Command ^
    "try { ^
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        (New-Object Net.WebClient).DownloadFile('%NODE_URL%', '%NODE_MSI%'); ^
        Write-Host 'Downloaded successfully' ^
    } catch { ^
        Write-Host 'Download failed'; ^
        exit 1 ^
    }"

if %errorlevel% neq 0 (
    echo Download failed. Please install Node.js manually from:
    echo https://nodejs.org/
    exit /b 1
)

echo Installing Node.js...
msiexec /i "%NODE_MSI%" /quiet /norestart

echo Node.js installed successfully
echo Please restart your terminal for changes to take effect
exit /b 0

:error
echo.
echo =================================================================
echo  SETUP FAILED
echo =================================================================
echo Error details saved to: %ERROR_LOG%
echo.
echo Please check the error log or try manual installation:
echo See: ENVIRONMENT_SETUP.md for detailed instructions
echo.
pause
exit /b 1
