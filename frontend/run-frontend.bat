@echo off
chcp 65001 >nul
title 校园助手-前端 (Vite)

:: 切换到脚本所在目录（frontend 目录）
cd /d "%~dp0"

echo ===========================================
echo  校园助手 - 前端服务启动中...
echo ===========================================
echo.
echo [1/2] 安装依赖...

:: 检查 node_modules 是否存在
if not exist "node_modules" (
    echo   首次运行，正在安装前端依赖...
    call npm install
) else (
    echo   依赖已安装，跳过安装步骤
)

echo.
echo [2/2] 启动前端开发服务器...
echo       端口: 5173
echo.

:: 启动 Vite 开发服务器
call npm run dev

echo.
echo 前端服务已停止。
pause
