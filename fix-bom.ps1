# 移除 Java 文件的 BOM 字符
# PowerShell 脚本

$ErrorActionPreference = "Stop"

Write-Host "正在移除 Java 文件的 BOM 字符..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path "java17(1.20.1-1.20.6)\fabric+forge=1.20.1" -Filter "*.java" -Recurse

$fixedCount = 0

foreach ($file in $javaFiles) {
    try {
        # 读取文件内容（作为字节）
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # 检查是否有 BOM (UTF-8 BOM: EF BB BF)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            # 移除 BOM（跳过前3个字节）
            $contentWithoutBom = $bytes[3..($bytes.Length - 1)]
            
            # 写入文件（UTF-8 without BOM）
            [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom)
            
            Write-Host "已修复: $($file.FullName)" -ForegroundColor Green
            $fixedCount++
        }
    }
    catch {
        Write-Host "处理文件失败: $($file.FullName) - $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "完成！修复了 $fixedCount 个文件" -ForegroundColor Cyan

