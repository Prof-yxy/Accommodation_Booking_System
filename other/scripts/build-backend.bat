@echo off
REM Maven build with alternative repository settings
REM This script bypasses network issues by using cached dependencies

setlocal enabledelayedexpansion

echo.
echo ===================================================
echo  Backend Build - Network Resilient
echo ===================================================
echo.

cd /d "%~dp0\backend"

echo Attempting to build with Maven...
echo.

REM Try with updated pom.xml that includes Aliyun mirror
mvn clean compile -q

if errorlevel 1 (
    echo.
    echo Build attempt 1 failed. Trying with offline mode using cached dependencies...
    mvn clean compile -o 2>nul
    
    if errorlevel 1 (
        echo.
        echo Offline mode failed. This means Maven cache is empty.
        echo Please check your internet connection and try again:
        echo   cd backend
        echo   mvn clean install -DskipTests -U
        pause
        exit /b 1
    ) else (
        echo Using cached dependencies successful!
    )
)

echo.
echo Packaging application...
mvn package -DskipTests -q

if errorlevel 1 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo ===================================================
echo  Build completed successfully!
echo ===================================================
echo.
echo Next: Run start.bat from project root
pause
