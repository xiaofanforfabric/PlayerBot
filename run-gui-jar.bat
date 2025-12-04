@echo off
REM FanMacro GUI 独立运行脚本 (Windows)
REM 使用包含所有依赖的 Fat JAR，无需 Gradle

chcp 65001 >nul
echo ========================================
echo   FanMacro 积木式编程 GUI (独立运行)
echo ========================================
echo.

REM 检查 Java 版本
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Java，请安装 Java 17 或更高版本
    pause
    exit /b 1
)

REM 检查 JAR 文件是否存在
if not exist "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar" (
    echo 错误: 未找到 Fat JAR 文件
    echo 请先运行: gradlew.bat build
    pause
    exit /b 1
)

REM 运行 Fat JAR（包含所有依赖，可独立运行）
echo 正在启动 GUI（使用 Fat JAR）...
echo.
java -jar "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar"

if errorlevel 1 (
    echo.
    echo 启动失败，请检查错误信息
    echo.
    echo 提示: 如果遇到问题，请确保使用 Java 17 或更高版本
    pause
    exit /b 1
)

pause
