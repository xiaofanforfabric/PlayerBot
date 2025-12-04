@echo off
chcp 65001 >nul
echo ========================================
echo   批量移除所有 Java 文件的 UTF-8 BOM
echo ========================================
echo.

REM 移除 common 模块的 BOM
powershell -Command "$files = Get-ChildItem -Path 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1\common\src\main\java' -Filter '*.java' -Recurse; $count = 0; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.Name)\" -ForegroundColor Green; $count++ } } catch { } }; Write-Host \"Common 模块: 处理 $count 个文件\" -ForegroundColor Cyan"

REM 移除 fabric-like 模块的 BOM
powershell -Command "$files = Get-ChildItem -Path 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1\fabric-like\src\main\java' -Filter '*.java' -Recurse; $count = 0; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.Name)\" -ForegroundColor Green; $count++ } } catch { } }; Write-Host \"Fabric-like 模块: 处理 $count 个文件\" -ForegroundColor Cyan"

REM 移除 fabric 模块的 BOM
powershell -Command "$files = Get-ChildItem -Path 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1\fabric\src\main\java' -Filter '*.java' -Recurse; $count = 0; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.Name)\" -ForegroundColor Green; $count++ } } catch { } }; Write-Host \"Fabric 模块: 处理 $count 个文件\" -ForegroundColor Cyan"

REM 移除 forge 模块的 BOM
powershell -Command "$files = Get-ChildItem -Path 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1\forge\src\main\java' -Filter '*.java' -Recurse; $count = 0; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.Name)\" -ForegroundColor Green; $count++ } } catch { } }; Write-Host \"Forge 模块: 处理 $count 个文件\" -ForegroundColor Cyan"

REM 移除 quilt 模块的 BOM
powershell -Command "$files = Get-ChildItem -Path 'java17(1.20.1-1.20.6)\fabric+forge=1.20.1\quilt\src\main\java' -Filter '*.java' -Recurse; $count = 0; foreach ($file in $files) { try { $bytes = [System.IO.File]::ReadAllBytes($file.FullName); if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $contentWithoutBom = $bytes[3..($bytes.Length - 1)]; [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom); Write-Host \"已移除 BOM: $($file.Name)\" -ForegroundColor Green; $count++ } } catch { } }; Write-Host \"Quilt 模块: 处理 $count 个文件\" -ForegroundColor Cyan"

echo.
echo ========================================
echo   完成！所有 BOM 已移除
echo ========================================
pause

