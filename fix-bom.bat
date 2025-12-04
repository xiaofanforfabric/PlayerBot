@echo off
chcp 65001 >nul
echo ========================================
echo   移除 Java 文件的 UTF-8 BOM 字符
echo ========================================
echo.

REM 使用 PowerShell 移除所有 Java 文件的 BOM
powershell -Command "$basePath = 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1'; $dirs = @('common', 'fabric', 'forge', 'quilt', 'fabric-like'); $totalCount = 0; foreach ($dir in $dirs) { $javaPath = Join-Path $basePath \"$dir\src\main\java\"; if (Test-Path $javaPath) { $files = Get-ChildItem -Path $javaPath -Filter '*.java' -Recurse -ErrorAction SilentlyContinue; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.FullName.Replace((Get-Location).Path + '\', ''))\" -ForegroundColor Green; $totalCount++ } } catch { Write-Host \"处理失败: $($file.FullName)\" -ForegroundColor Red } } } }; Write-Host \"`n处理完成！共处理 $totalCount 个文件\" -ForegroundColor Cyan"

echo.
echo ========================================
echo   完成！
echo ========================================
pause

