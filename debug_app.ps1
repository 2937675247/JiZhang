# jizhang一键调试脚本
# 设置工作目录为项目根目录
$env:JAVA_HOME = "D:\Program Files\JDK17"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
Set-Location -Path "D:\Desktop\记账\jizhang1"

# 定义ADB路径
$adbPath = "D:\Desktop\记账\jizhang1\adb\adb.exe"
$packageName = "com.example.jizhang"
$mainActivity = ".MainActivity"
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

# 显示调试菜单
function Show-Menu {
    Clear-Host
    Write-Host "===== 记账应用一键调试工具 =====" -ForegroundColor Cyan
    Write-Host "1. 构建应用" -ForegroundColor Green
    Write-Host "2. 安装应用到设备" -ForegroundColor Green
    Write-Host "3. 启动应用" -ForegroundColor Green
    Write-Host "4. 查看应用日志" -ForegroundColor Green
    Write-Host "5. 停止应用" -ForegroundColor Green
    Write-Host "6. 清除应用数据" -ForegroundColor Green
    Write-Host "7. 截图" -ForegroundColor Green
    Write-Host "8. 执行完整调试流程(构建+安装+启动+查看日志)" -ForegroundColor Yellow
    Write-Host "9. 查看设备连接状态" -ForegroundColor Green
    Write-Host "0. 退出" -ForegroundColor Red
    Write-Host "=================================" -ForegroundColor Cyan
}

# 检查设备连接状态
function Check-DeviceConnection {
    Write-Host "正在检查设备连接状态..." -ForegroundColor Cyan
    $devices = & $adbPath devices
    if ($devices -match "device$") {
        Write-Host "设备已连接!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "没有设备连接，请连接设备并启用USB调试。" -ForegroundColor Red
        return $false
    }
}

# 构建应用
function Build-App {
    Write-Host "正在构建应用..." -ForegroundColor Cyan
    try {
        .\gradlew.bat assembleDebug
        if ($LASTEXITCODE -eq 0) {
            Write-Host "应用构建成功!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "应用构建失败!" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "构建过程发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 安装应用到设备
function Install-App {
    if (-not (Check-DeviceConnection)) { return $false }
    
    Write-Host "正在安装应用到设备..." -ForegroundColor Cyan
    try {
        $result = & $adbPath install -r $apkPath
        if ($result -match "Success") {
            Write-Host "应用安装成功!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "应用安装失败: $result" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "安装过程发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 启动应用
function Start-App {
    if (-not (Check-DeviceConnection)) { return $false }
    
    Write-Host "正在启动应用..." -ForegroundColor Cyan
    try {
        & $adbPath shell am start -n "$packageName/$mainActivity"
        Write-Host "应用已启动!" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "启动应用发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 停止应用
function Stop-App {
    if (-not (Check-DeviceConnection)) { return $false }
    
    Write-Host "正在停止应用..." -ForegroundColor Cyan
    try {
        & $adbPath shell am force-stop $packageName
        Write-Host "应用已停止!" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "停止应用发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 清除应用数据
function Clear-AppData {
    if (-not (Check-DeviceConnection)) { return $false }
    
    Write-Host "正在清除应用数据..." -ForegroundColor Cyan
    try {
        & $adbPath shell pm clear $packageName
        Write-Host "应用数据已清除!" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "清除应用数据发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 查看应用日志
function Show-AppLogs {
    if (-not (Check-DeviceConnection)) { return $false }
    
    Write-Host "正在查看应用日志 (按Ctrl+C退出)..." -ForegroundColor Cyan
    try {
        Write-Host "========== 应用日志开始 ==========" -ForegroundColor Yellow
        & $adbPath logcat -c
        & $adbPath logcat | Select-String $packageName
    } catch {
        Write-Host "查看日志发生错误: $_" -ForegroundColor Red
    }
}

# 截图功能
function Take-Screenshot {
    if (-not (Check-DeviceConnection)) { return $false }
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $screenshotName = "screenshot_$timestamp.png"
    $tempPath = "/sdcard/$screenshotName"
    $localPath = ".\$screenshotName"
    
    Write-Host "正在获取截图..." -ForegroundColor Cyan
    try {
        & $adbPath shell screencap -p $tempPath
        & $adbPath pull $tempPath $localPath
        & $adbPath shell rm $tempPath
        
        Write-Host "截图已保存至: $localPath" -ForegroundColor Green
        # 自动打开截图
        Invoke-Item $localPath
        return $true
    } catch {
        Write-Host "截图过程发生错误: $_" -ForegroundColor Red
        return $false
    }
}

# 完整的调试流程
function Start-FullDebugProcess {
    $buildSuccess = Build-App
    if (-not $buildSuccess) { 
        Read-Host "按Enter继续..."
        return 
    }
    
    $installSuccess = Install-App
    if (-not $installSuccess) { 
        Read-Host "按Enter继续..."
        return 
    }
    
    $startSuccess = Start-App
    if (-not $startSuccess) { 
        Read-Host "按Enter继续..."
        return 
    }
    
    # 等待应用启动
    Start-Sleep -Seconds 2
    
    # 查看日志
    Show-AppLogs
}

# 主程序循环
do {
    Show-Menu
    $selection = Read-Host "请选择操作"
    
    switch ($selection) {
        '1' { 
            Build-App
            Read-Host "按Enter继续..."
        }
        '2' { 
            Install-App
            Read-Host "按Enter继续..."
        }
        '3' { 
            Start-App
            Read-Host "按Enter继续..."
        }
        '4' { 
            Show-AppLogs
        }
        '5' { 
            Stop-App
            Read-Host "按Enter继续..."
        }
        '6' { 
            Clear-AppData
            Read-Host "按Enter继续..."
        }
        '7' { 
            Take-Screenshot
            Read-Host "按Enter继续..."
        }
        '8' { 
            Start-FullDebugProcess
        }
        '9' { 
            Check-DeviceConnection
            Read-Host "按Enter继续..."
        }
        '0' { 
            return 
        }
        default { 
            Write-Host "无效选择，请重新输入。" -ForegroundColor Red
            Read-Host "按Enter继续..."
        }
    }
} while ($selection -ne '0') 