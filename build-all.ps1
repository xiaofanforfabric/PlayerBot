# PlayerBot 项目编译脚本
# 同时编译主Java项目和1.20.1混合模组项目

param(
    [switch]$Clean = $false,
    [switch]$Build = $true,
    [switch]$Test = $false
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PlayerBot 项目一键编译脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "将编译以下项目:" -ForegroundColor Yellow
Write-Host "  1. GUI 客户端 (FanMacro GUI)" -ForegroundColor Gray
Write-Host "  2. AI 服务器 (FanMacro AI Server)" -ForegroundColor Gray
Write-Host "  3. Minecraft 模组 (1.20.1)" -ForegroundColor Gray
Write-Host ""
Write-Host "开始编译..." -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录
$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) {
    $ScriptDir = Get-Location
}

# 项目路径
$GuiProjectPath = $ScriptDir
$ServerProjectPath = Join-Path $ScriptDir "PlayerBot-Macro-GUI-server"
$ModProjectPath = Join-Path $ScriptDir "java17(1.20.1-1.20.6)\fabric+forge=1.20.1"

# 检查项目路径是否存在
if (-not (Test-Path $ModProjectPath)) {
    Write-Host "错误: 找不到混合模组项目路径: $ModProjectPath" -ForegroundColor Red
    exit 1
}

# 函数：执行 Gradle 命令
function Invoke-Gradle {
    param(
        [string]$ProjectPath,
        [string]$ProjectName,
        [string[]]$Tasks
    )
    
    Write-Host ""
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    Write-Host "  编译项目: $ProjectName" -ForegroundColor Yellow
    Write-Host "  路径: $ProjectPath" -ForegroundColor Yellow
    Write-Host "  任务: $($Tasks -join ', ')" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    
    Push-Location $ProjectPath
    
    try {
        $gradlew = if ($IsWindows -or $env:OS -like "*Windows*") { ".\gradlew.bat" } else { ".\gradlew" }
        
        if (-not (Test-Path $gradlew)) {
            Write-Host "错误: 找不到 Gradle Wrapper: $gradlew" -ForegroundColor Red
            return $false
        }
        
        $taskList = $Tasks -join " "
        Write-Host "执行: $gradlew $taskList" -ForegroundColor Gray
        Write-Host ""
        
        & $gradlew $Tasks
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host ""
            Write-Host "错误: $ProjectName 编译失败 (退出代码: $LASTEXITCODE)" -ForegroundColor Red
            return $false
        }
        
        Write-Host ""
        Write-Host "✓ $ProjectName 编译成功" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host ""
        Write-Host "错误: $ProjectName 编译时发生异常: $_" -ForegroundColor Red
        return $false
    }
    finally {
        Pop-Location
    }
}

# 编译任务列表
$allSuccess = $true
$successCount = 0
$failCount = 0
$skipCount = 0

# 1. 编译 GUI 客户端项目
if ($Build) {
    $guiTasks = @()
    
    if ($Clean) {
        $guiTasks += "clean"
    }
    
    $guiTasks += "build"
    
    if (Test-Path (Join-Path $GuiProjectPath "gradlew.bat")) {
        if (Invoke-Gradle -ProjectPath $GuiProjectPath -ProjectName "GUI 客户端" -Tasks $guiTasks) {
            $successCount++
        } else {
            $failCount++
            $allSuccess = $false
        }
    } else {
        Write-Host "[跳过] 找不到 gradlew.bat，跳过 GUI 客户端编译" -ForegroundColor Yellow
        $skipCount++
    }
}

# 2. 编译 AI 服务器项目
if ($Build) {
    $serverTasks = @()
    
    if ($Clean) {
        $serverTasks += "clean"
    }
    
    $serverTasks += "build"
    
    if (Test-Path (Join-Path $ServerProjectPath "gradlew.bat")) {
        if (Invoke-Gradle -ProjectPath $ServerProjectPath -ProjectName "AI 服务器" -Tasks $serverTasks) {
            $successCount++
        } else {
            $failCount++
            $allSuccess = $false
        }
    } else {
        Write-Host "[跳过] 找不到 gradlew.bat，跳过 AI 服务器编译" -ForegroundColor Yellow
        $skipCount++
    }
}

# 3. 编译 Minecraft 模组项目（Fabric、Forge、Quilt）
if ($Build) {
    $modTasks = @()
    
    if ($Clean) {
        $modTasks += "clean"
    }
    
    # 编译所有平台的模组
    $modTasks += "build"
    
    # 也可以分别编译各个平台：
    # $modTasks += ":fabric:build"
    # $modTasks += ":forge:build"
    # $modTasks += ":quilt:build"
    
    if (Test-Path (Join-Path $ModProjectPath "gradlew.bat")) {
        if (Invoke-Gradle -ProjectPath $ModProjectPath -ProjectName "Minecraft 模组 (1.20.1)" -Tasks $modTasks) {
            $successCount++
        } else {
            $failCount++
            $allSuccess = $false
        }
    } else {
        Write-Host "[跳过] 找不到 gradlew.bat，跳过 Minecraft 模组编译" -ForegroundColor Yellow
        $skipCount++
    }
}

# 4. 运行测试（可选）
if ($Test) {
    Write-Host ""
    Write-Host "运行测试..." -ForegroundColor Cyan
    
    if (-not (Invoke-Gradle -ProjectPath $GuiProjectPath -ProjectName "GUI 客户端" -Tasks @("test"))) {
        $allSuccess = $false
    }
}

# 输出结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  编译完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "编译统计:" -ForegroundColor Yellow
Write-Host "  成功: $successCount 个项目" -ForegroundColor Green
Write-Host "  失败: $failCount 个项目" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Gray" })
Write-Host "  跳过: $skipCount 个项目" -ForegroundColor Gray
Write-Host ""

if ($failCount -gt 0) {
    Write-Host "⚠️  有项目编译失败，请检查错误信息" -ForegroundColor Red
    Write-Host ""
} else {
    Write-Host "✅ 所有项目编译成功！" -ForegroundColor Green
    Write-Host ""
}

Write-Host "编译输出位置:" -ForegroundColor Cyan
Write-Host "  GUI 客户端:     $GuiProjectPath\build\libs\" -ForegroundColor Gray
Write-Host "  AI 服务器:      $ServerProjectPath\build\libs\" -ForegroundColor Gray
Write-Host "  Fabric 模组:    $ModProjectPath\fabric\build\libs\" -ForegroundColor Gray
Write-Host "  Forge 模组:     $ModProjectPath\forge\build\libs\" -ForegroundColor Gray
Write-Host "  Quilt 模组:     $ModProjectPath\quilt\build\libs\" -ForegroundColor Gray
Write-Host ""

# 显示生成的 JAR 文件
Write-Host "生成的 JAR 文件:" -ForegroundColor Cyan
if (Test-Path (Join-Path $GuiProjectPath "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar")) {
    Write-Host "  ✓ GUI 客户端: PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar" -ForegroundColor Green
}
if (Test-Path (Join-Path $ServerProjectPath "build\libs\PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar")) {
    Write-Host "  ✓ AI 服务器:  PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar" -ForegroundColor Green
}
if (Test-Path (Join-Path $ModProjectPath "fabric\build\libs\*.jar")) {
    Write-Host "  ✓ Fabric 模组: fabric\build\libs\*.jar" -ForegroundColor Green
}
if (Test-Path (Join-Path $ModProjectPath "forge\build\libs\*.jar")) {
    Write-Host "  ✓ Forge 模组:  forge\build\libs\*.jar" -ForegroundColor Green
}
if (Test-Path (Join-Path $ModProjectPath "quilt\build\libs\*.jar")) {
    Write-Host "  ✓ Quilt 模组:  quilt\build\libs\*.jar" -ForegroundColor Green
}
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if (-not $allSuccess) {
    exit 1
}

