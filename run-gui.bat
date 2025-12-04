@echo off
REM FanMacro GUI 启动脚本 (Windows)

chcp 65001 >nul
echo ========================================
echo   FanMacro 积木式编程 GUI
echo ========================================
echo.

REM 检查 Java 版本
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Java，请安装 Java 17 或更高版本
    pause
    exit /b 1
)

REM 运行 GUI
echo 正在启动 GUI...
echo.
call gradlew.bat run

if errorlevel 1 (
    echo.
    echo 启动失败，请检查错误信息
    pause
    exit /b 1
)

pause

