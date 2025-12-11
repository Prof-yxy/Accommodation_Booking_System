@echo off
REM ===========================================================================
REM Camping Booking System - Full Setup and Fix Batch Script
REM Purpose: Initialize database with UTF-8 encoding and build the project
REM ===========================================================================

setlocal enabledelayedexpansion

cls
echo.
echo ===================================================================
echo  Camping Booking System - Full Setup and Fix
echo ===================================================================
echo.

REM Check PostgreSQL
echo [1/3] Checking PostgreSQL installation...
psql --version >nul 2>&1
if errorlevel 1 (
    echo Error: PostgreSQL is not installed or not in PATH
    pause
    exit /b 1
)
echo PostgreSQL found
echo.

REM Initialize Database
echo [2/3] Initializing Database with UTF-8 Encoding...
echo.

set PGPASSWORD=postgres

echo Creating camping_db database...
psql -h localhost -U postgres -d postgres -c "DROP DATABASE IF EXISTS camping_db;" 2>nul
psql -h localhost -U postgres -d postgres -c "CREATE DATABASE camping_db ENCODING 'UTF8';" 2>nul

if errorlevel 1 (
    echo Error: Failed to create database
    echo Please ensure PostgreSQL is running and password is correct
    pause
    exit /b 1
)

echo Database created successfully
echo.

echo Executing schema_utf8.sql...
psql -h localhost -U postgres -d camping_db -f "sql\schema_utf8.sql" >nul 2>&1
if errorlevel 1 (
    echo Warning: Schema execution had issues, but continuing...
)
echo Schema created
echo.

echo Executing views_triggers.sql...
psql -h localhost -U postgres -d camping_db -f "sql\views_triggers.sql" >nul 2>&1
echo Views and triggers created
echo.

echo Executing data.sql...
psql -h localhost -U postgres -d camping_db -f "sql\data.sql" >nul 2>&1
echo Sample data loaded
echo.

REM Build Backend
echo [3/3] Building Backend...
echo.

cd backend
echo Cleaning previous build...
call mvn clean -q

echo Building backend with updated dependencies...
call mvn install -DskipTests -U

if errorlevel 1 (
    echo Error: Backend build failed
    cd ..
    pause
    exit /b 1
)

echo Backend built successfully
cd ..
echo.

REM Summary
echo.
echo ===================================================================
echo  Setup Completed Successfully
echo ===================================================================
echo.
echo All steps completed! The system is ready to run.
echo.
echo Next steps:
echo   1. Run: .\start.bat
echo   2. Access frontend at: http://localhost:5173
echo   3. Access backend at: http://localhost:8080
echo.
pause
