# 移除 Java 文件的 UTF-8 BOM 字符
# 解决编译错误：非法字符 '\ufeff'

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  移除 Java 文件的 UTF-8 BOM 字符" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$javaDir = "java17(1.20.1-1.20.6)\fabric+forge=1.20.1\common\src\main\java"
$files = Get-ChildItem -Path $javaDir -Filter "*.java" -Recurse

$count = 0
foreach ($file in $files) {
    try {
        # 读取文件内容（作为字节）
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # 检查是否有 BOM (UTF-8 BOM = EF BB BF)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            # 移除 BOM（跳过前3个字节）
            $contentWithoutBom = $bytes[3..($bytes.Length - 1)]
            [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom)
            Write-Host "已移除 BOM: $($file.FullName)" -ForegroundColor Green
            $count++
        }
    } catch {
        Write-Host "处理文件失败: $($file.FullName) - $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  处理完成！共处理 $count 个文件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

