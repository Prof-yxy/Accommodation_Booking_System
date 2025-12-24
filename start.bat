@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

echo.
echo ==========================================
echo   Camping Booking System - Quick Start
echo ==========================================
echo.

REM Check if Java is available
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 17 or later
    pause
    exit /b 1
)
echo [OK] Java found

REM Check if Maven is available
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven 3.6 or later
    pause
    exit /b 1
)
echo [OK] Maven found

REM Check if Node.js is available
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed or not in PATH
    echo Please install Node.js 18 or later
    pause
    exit /b 1
)
echo [OK] Node.js found

REM Check if PostgreSQL is running
echo.
echo Checking PostgreSQL connection...
pg_isready -h localhost -p 5432 >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] PostgreSQL may not be running on localhost:5432
    echo Please ensure PostgreSQL is started and camping_db exists
    echo.
    echo To start PostgreSQL:
    echo   - Windows Service: net start postgresql-x64-XX
    echo   - Or start from pgAdmin / Services panel
    echo.
    set /p continue="Continue anyway? (y/n): "
    if /i not "!continue!"=="y" exit /b 1
) else (
    echo [OK] PostgreSQL is running
)
echo.

REM ==========================================
REM Build Backend
REM ==========================================
echo [1/4] Building backend...
cd backend
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed
    cd ..
    pause
    exit /b 1
)
cd ..
echo [OK] Backend built successfully
echo.

REM ==========================================
REM Install Frontend Dependencies (if needed)
REM ==========================================
echo [2/4] Checking frontend dependencies...
cd frontend
if not exist "node_modules" (
    echo Installing npm packages...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] Frontend npm install failed
        cd ..
        pause
        exit /b 1
    )
)
cd ..
echo [OK] Frontend dependencies ready
echo.

REM ==========================================
REM Start Backend Service
REM ==========================================
echo [3/4] Starting backend service...

REM Use cmd /k to keep window open for debugging
start "Camping Backend" cmd /k "cd /d %~dp0backend && echo Starting Spring Boot... && java -jar target\camping-booking-system-1.0.0.jar"

REM Wait for backend to start
echo Waiting for backend to initialize (this may take 30-60 seconds)...
set /a count=0
:wait_backend
timeout /t 3 /nobreak >nul
set /a count+=1

REM Check if backend is responding
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/type/list 2>nul | findstr "200" >nul
if %errorlevel% equ 0 (
    echo.
    echo [OK] Backend started at http://localhost:8080/api
    goto backend_ready
)

REM Check if process is still running by checking port
netstat -an | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %errorlevel% neq 0 (
    if %count% geq 5 (
        echo.
        echo [ERROR] Backend failed to start!
        echo Please check the backend window for error messages.
        echo.
        echo Common issues:
        echo   1. PostgreSQL not running
        echo   2. Database 'camping_db' does not exist
        echo   3. Wrong database password in application.yml
        echo   4. Port 8080 already in use
        echo.
        pause
        exit /b 1
    )
)

if %count% geq 20 (
    echo.
    echo [WARNING] Backend taking too long, continuing...
    goto backend_ready
)
echo    Waiting... [%count%/20]
goto wait_backend

:backend_ready
echo.

REM ==========================================
REM Start Frontend Service
REM ==========================================
echo [4/4] Starting frontend service...
start "Camping Frontend" cmd /c "cd frontend && npm run dev"

timeout /t 5 /nobreak >nul

echo.
echo ==========================================
echo   SUCCESS: Application Started!
echo ==========================================
echo.
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080/api
echo   Database: localhost:5432/camping_db
echo.
echo   Test Accounts:
echo     Admin:  admin / admin123
echo     User:   user1 / password
echo.
echo   Press any key to open browser...
echo   Close terminal windows to stop services
echo.

pause >nul
start http://localhost:5173

echo.
echo Application is running. Press any key to exit this window.
pause >nul
