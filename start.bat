@echo off
chcp 65001 >nul
title 校园智能助手 - 一键启动

:: 使用 PowerShell 脚本启动（更可靠，支持端口检测和自动等待）
powershell.exe -ExecutionPolicy Bypass -NoProfile -File "%~dp0start.ps1"

pause
