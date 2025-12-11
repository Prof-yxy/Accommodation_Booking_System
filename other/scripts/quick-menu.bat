@echo off
REM 快速启动 - 仅需一步！

setlocal enabledelayedexpansion

cd /d "%~dp0"

:menu
cls
echo.
echo ===============================================================
echo  Camping Booking System - 快速启动菜单
echo ===============================================================
echo.
echo 请选择要执行的操作：
echo.
echo [1] 一键完全修复（推荐）
echo     - 重置数据库
echo     - 清理Maven缓存
echo     - 重新构建系统
echo.
echo [2] 仅重置数据库
echo     - 用于修复数据库问题
echo.
echo [3] 仅构建后端
echo     - 用于修复Maven问题
echo.
echo [4] 启动系统
echo     - start.bat
echo.
echo [5] 打开项目文件夹
echo.
echo [0] 退出
echo.

set /p choice="请输入选择 [0-5]: "

if "%choice%"=="1" (
    call complete-fix.bat
    goto menu
) else if "%choice%"=="2" (
    cd sql
    call reset-db.bat
    cd ..
    goto menu
) else if "%choice%"=="3" (
    call build-backend.bat
    goto menu
) else if "%choice%"=="4" (
    call start.bat
    goto menu
) else if "%choice%"=="5" (
    start explorer "%cd%"
    goto menu
) else if "%choice%"=="0" (
    exit /b 0
) else (
    echo 无效的选择
    timeout /t 2 /nobreak
    goto menu
)
