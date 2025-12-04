@echo off
REM FanMacro GUI 独立运行脚本 (Windows) - 修复版
REM 自动提取 JavaFX 模块并配置模块路径

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

REM 创建临时目录用于提取 JavaFX 模块
set "TEMP_JAVAFX=%TEMP%\javafx-modules-%RANDOM%"
mkdir "%TEMP_JAVAFX%" >nul 2>&1

REM 尝试从 Gradle 缓存中提取 JavaFX 模块
set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "JAVAFX_PATH="

REM 查找 JavaFX 模块（从 Gradle 缓存）
for /r "%GRADLE_USER_HOME%\caches\modules-2\files-2.1\org.openjfx" %%i in (javafx-controls-17.0.2-win.jar) do (
    set "JAVAFX_PATH=%%~dpi"
    goto :found
)

:found
if "%JAVAFX_PATH%"=="" (
    echo 警告: 未找到 JavaFX 模块，尝试直接运行...
    echo.
    java -jar "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar"
    if errorlevel 1 (
        echo.
        echo 错误: 无法启动应用
        echo.
        echo 解决方案:
        echo 1. 使用 Gradle 运行: gradlew.bat run
        echo 2. 或下载 JavaFX SDK 并使用以下命令:
        echo    java --module-path ^<javafx-sdk^>/lib --add-modules javafx.controls,javafx.fxml -jar build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
        pause
        exit /b 1
    )
) else (
    echo 找到 JavaFX 模块: %JAVAFX_PATH%
    echo 正在启动应用...
    echo.
    
    REM 构建模块路径（包含所有 JavaFX 模块的父目录）
    for %%i in ("%JAVAFX_PATH%") do set "MODULE_PATH=%%~dpi"
    
    REM 运行应用
    java --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -jar "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar"
    
    if errorlevel 1 (
        echo.
        echo 启动失败，尝试使用兼容模式...
        java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar "build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar"
    )
)

REM 清理临时目录
rmdir /s /q "%TEMP_JAVAFX%" >nul 2>&1

pause

