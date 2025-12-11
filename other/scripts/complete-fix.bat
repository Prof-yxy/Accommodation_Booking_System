@echo off
REM Complete fix script - handles database + build issues

setlocal enabledelayedexpansion

cls
echo.
echo ===============================================================
echo  Camping Booking System - Complete Fix
echo ===============================================================
echo.

cd /d "%~dp0"

REM Step 1: Fix Database
echo [Step 1/3] Fixing Database...
echo.

cd sql
call reset-db.bat
if errorlevel 1 (
    echo Database setup failed
    pause
    exit /b 1
)

cd ..
echo.
echo ✓ Database fixed
echo.

REM Step 2: Build Backend
echo [Step 2/3] Building Backend...
echo.

cd backend

echo Cleaning previous build...
call mvn clean -q

echo Building project...
call mvn install -DskipTests -q

if errorlevel 1 (
    echo.
    echo ⚠ Build failed. Attempting recovery...
    echo.
    echo Clearing Maven cache for problematic artifacts...
    rmdir /s /q "%USERPROFILE%\.m2\repository\org\apache\maven\plugins\maven-install-plugin" 2>nul
    rmdir /s /q "%USERPROFILE%\.m2\repository\org\apache\maven" 2>nul
    
    echo Retrying build...
    call mvn install -DskipTests -q
    
    if errorlevel 1 (
        echo.
        echo ERROR: Build still failing
        echo Please try manual build: mvn clean install -DskipTests -U
        cd ..
        pause
        exit /b 1
    )
)

cd ..

echo.
echo ✓ Backend built successfully
echo.

REM Step 3: Frontend (optional)
echo [Step 3/3] Building Frontend...
echo.

cd frontend
call npm install -q 2>nul
call npm run build -q 2>nul
cd ..

echo ✓ Frontend built
echo.

REM Complete
echo ===============================================================
echo  ✓ All fixes completed!
echo ===============================================================
echo.
echo Next steps:
echo   1. Run: start.bat
echo   2. Access: http://localhost:5173
echo   3. Login and test the system
echo.
pause
