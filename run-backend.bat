@echo off
chcp 65001 >nul
title 校园助手-后端 (Spring Boot)

:: 切换到脚本所在目录（项目根目录）
cd /d "%~dp0"

echo ===========================================
echo  校园助手 - 后端服务启动中...
echo ===========================================
echo.

:: 设置 DashScope API Key（如果未设置环境变量）
if "%DASHSCOPE_API_KEY%"=="" (
    set DASHSCOPE_API_KEY=sk-placeholder0000000000000000000000
    echo [提示] 未设置 DASHSCOPE_API_KEY 环境变量
    echo       使用占位符启动，AI 问答功能可能不可用
    echo       设置方式: set DASHSCOPE_API_KEY=你的真实Key
    echo.
)

echo [1/2] 编译并启动后端...
echo       Java:  25
echo       端口:  8080
echo       路径:  /api
echo.

:: 用 Maven 编译并启动 Spring Boot
call mvn spring-boot:run

echo.
echo 后端服务已停止。
pause
