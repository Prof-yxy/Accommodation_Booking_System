@echo off
setlocal enabledelayedexpansion

echo.
echo ==========================================
echo Camping Booking System - Quick Start
echo ==========================================
echo.

REM Build backend
echo Building backend...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Backend build failed
    cd ..
    pause
    exit /b 1
)
cd ..
echo OK: Backend built
echo.

REM Start backend
echo Starting backend service...
start "Camping Backend" cmd /k ^
java -jar backend\target\camping-booking-system-1.0.0.jar

timeout /t 10 /nobreak
echo OK: Backend started at http://localhost:8080/api
echo.

REM Start frontend
echo Starting frontend...
cd frontend
start "Camping Frontend" cmd /k npm run dev
cd ..

timeout /t 5 /nobreak

echo.
echo ==========================================
echo SUCCESS: Application started!
echo ==========================================
echo.
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080/api
echo Database: localhost:5432/camping_db
echo.
echo Test account:
echo   Username: user1
echo   Password: (empty)
echo.
echo Close console windows to stop services
echo.

pause
