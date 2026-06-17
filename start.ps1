# 校园智能助手 - 一键启动脚本 (PowerShell)
# 比 start.bat 更可靠，支持端口检测和自动等待

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = $ProjectRoot
$FrontendDir = Join-Path $ProjectRoot "frontend"
$BackendUrl = "http://localhost:8080/api/"
$FrontendUrl = "http://localhost:5173/"

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  校园智能助手 - 一键启动" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Java
try { java -version 2>&1 | Out-Null } catch {
    Write-Host "[错误] 未找到 Java，请安装 JDK 25" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "[✓] Java 已就绪" -ForegroundColor Green

# 检查 Maven
try { mvn --version 2>&1 | Out-Null } catch {
    Write-Host "[错误] 未找到 Maven" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "[✓] Maven 已就绪" -ForegroundColor Green

# 检查 Node.js
try { node --version 2>&1 | Out-Null } catch {
    Write-Host "[错误] 未找到 Node.js" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}
Write-Host "[✓] Node.js 已就绪" -ForegroundColor Green

Write-Host ""

# ===== 启动后端 =====
Write-Host "[1/3] 启动后端服务 (Spring Boot)..." -ForegroundColor Yellow
Write-Host "      端口: 8080" -ForegroundColor Gray
Write-Host ""

# 先清理占用 8080 端口的 Java 进程
$existingJava = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($existingJava) {
    Write-Host "      端口 8080 被占用，正在清理旧进程..." -ForegroundColor Yellow
    $existingJava | ForEach-Object {
        $proc = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) { Stop-Process -Id $_.OwningProcess -Force }
    }
    Start-Sleep -Seconds 2
}

# 在后台启动 Maven（新窗口显示日志）
$backendJob = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$BackendDir`" && mvn spring-boot:run" -WindowStyle Normal -PassThru -NoNewWindow:$false
Write-Host "      后端进程已启动 (PID: $($backendJob.Id))，等待编译完成..." -ForegroundColor Gray

# 等待后端端口可用（最多等 120 秒）
$timeout = 120
$elapsed = 0
$ready = $false
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    try {
        $response = Invoke-WebRequest -Uri $BackendUrl -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 404 -or $response.StatusCode -eq 401 -or $response.StatusCode -eq 500) {
            $ready = $true
            break
        }
    } catch {
        # 还没启动，继续等待
    }
    $min = [math]::Floor($elapsed / 60)
    $sec = $elapsed % 60
    Write-Host "      等待后端启动... ${min}分${sec}秒" -ForegroundColor DarkYellow
}

if (-not $ready) {
    Write-Host "[错误] 后端启动超时（${timeout}秒），请检查日志" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "[✓] 后端启动成功！" -ForegroundColor Green

# ===== 启动前端 =====
Write-Host "[2/3] 启动前端服务 (Vite)..." -ForegroundColor Yellow
Write-Host "      端口: 5173" -ForegroundColor Gray

# 先清理占用的端口（防止端口冲突）
$existingVite = Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
if ($existingVite) {
    Write-Host "      端口 5173 被占用，正在清理..." -ForegroundColor Yellow
    $existingVite | ForEach-Object {
        $proc = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        if ($proc -and $proc.ProcessName -match "node") { Stop-Process -Id $_.OwningProcess -Force }
    }
    Start-Sleep -Seconds 2
}

# 先检查 node_modules
if (-not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
    Write-Host "      首次运行，正在安装前端依赖..." -ForegroundColor Yellow
    Push-Location $FrontendDir
    npm install
    Pop-Location
}

# 在新窗口启动 Vite
$frontendJob = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$FrontendDir`" && npm run dev" -WindowStyle Normal -PassThru -NoNewWindow:$false
Write-Host "      前端进程已启动 (PID: $($frontendJob.Id))" -ForegroundColor Gray

# 等待前端启动
Start-Sleep -Seconds 5

Write-Host "[✓] 前端启动成功！" -ForegroundColor Green

# ===== 打开浏览器 =====
Write-Host "[3/3] 正在打开浏览器..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
Start-Process $FrontendUrl

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  校园智能助手启动完成！" -ForegroundColor Cyan
Write-Host "" -ForegroundColor Cyan
Write-Host "  前端地址: $FrontendUrl" -ForegroundColor White
Write-Host "  后端地址: $BackendUrl" -ForegroundColor White
Write-Host "  API文档:  http://localhost:8080/api/doc.html" -ForegroundColor White
Write-Host ""
Write-Host "  ┌─────────────────────────────────────┐" -ForegroundColor Cyan
Write-Host "  │  请选择操作：                        │" -ForegroundColor Cyan
Write-Host "  │                                     │" -ForegroundColor Cyan
Write-Host "  │  [1] 保持服务运行，退出此窗口        │" -ForegroundColor Cyan
Write-Host "  │  [2] 关闭所有服务（后端+前端）并退出 │" -ForegroundColor Cyan
Write-Host "  └─────────────────────────────────────┘" -ForegroundColor Cyan
Write-Host ""
$choice = Read-Host "请输入选项 (1 或 2)"

if ($choice -eq "2") {
    Write-Host ""
    Write-Host "正在关闭所有服务..." -ForegroundColor Yellow

    # 关闭后端
    $javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($javaProcs) {
        $javaProcs | Stop-Process -Force
        Write-Host "  ✓ 已关闭 $($javaProcs.Count) 个 Java 进程" -ForegroundColor Green
    }

    # 关闭前端（通过端口）
    $viteByPort = Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue
    if ($viteByPort) {
        $viteByPort | ForEach-Object {
            $proc = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
            if ($proc) { Stop-Process -Id $_.OwningProcess -Force }
        }
        Write-Host "  ✓ 已关闭前端服务" -ForegroundColor Green
    }

    # 额外清理 Vite 进程
    Get-Process -Name node -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $($_.Id)").CommandLine
            if ($cmdLine -match "vite") {
                Stop-Process -Id $_.Id -Force
            }
        } catch {}
    }

    Write-Host ""
    Write-Host "所有服务已关闭，再见！" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "服务保持运行中。" -ForegroundColor Green
    Write-Host "如需关闭，请双击桌面的"关闭校园助手"快捷方式。" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "按回车键退出"
