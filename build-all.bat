@echo off
REM PlayerBot 项目一键编译脚本 (批处理版本)
REM 编译所有项目：GUI客户端、AI服务器、Minecraft模组

chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   PlayerBot 项目一键编译脚本
echo ========================================
echo.
echo 将编译以下项目:
echo   1. GUI 客户端 (FanMacro GUI)
echo   2. AI 服务器 (FanMacro AI Server)
echo   3. AllCommon 模块 (共用代码)
echo   4. Minecraft 模组 (1.20.1)
echo   5. Minecraft 模组 (1.20.4)
echo.
echo 开始编译...
echo.

REM 获取脚本所在目录
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

REM 项目路径
set "GUI_PROJECT=%SCRIPT_DIR%"
set "SERVER_PROJECT=%SCRIPT_DIR%PlayerBot-Macro-GUI-server"
set "ALLCOMMON_PROJECT=%SCRIPT_DIR%java17(1.20.1-1.20.6)\allcommon"
set "MOD_PROJECT_1201=%SCRIPT_DIR%java17(1.20.1-1.20.6)\fabric+forge=1.20.1"
set "MOD_PROJECT_1204=%SCRIPT_DIR%java17(1.20.1-1.20.6)\fabric+forge=1.20.4"

REM 编译结果统计
set "SUCCESS_COUNT=0"
set "FAIL_COUNT=0"
set "SKIP_COUNT=0"

REM ========================================
REM 1. 编译 GUI 客户端项目
REM ========================================
echo.
echo ========================================
echo [1/4] 编译项目: GUI 客户端
echo ========================================
cd /d "%GUI_PROJECT%"
if exist "gradlew.bat" (
    call gradlew.bat clean build --no-daemon
    if errorlevel 1 (
        echo [失败] GUI 客户端编译失败
        set /a FAIL_COUNT+=1
    ) else (
        echo [成功] GUI 客户端编译完成
        set /a SUCCESS_COUNT+=1
    )
) else (
    echo [跳过] 找不到 gradlew.bat，跳过 GUI 客户端编译
    set /a SKIP_COUNT+=1
)

REM ========================================
REM 2. 编译 AI 服务器项目
REM ========================================
echo.
echo ========================================
echo [2/4] 编译项目: AI 服务器
echo ========================================
cd /d "%SERVER_PROJECT%"
if exist "gradlew.bat" (
    call gradlew.bat clean build --no-daemon
    if errorlevel 1 (
        echo [失败] AI 服务器编译失败
        set /a FAIL_COUNT+=1
    ) else (
        echo [成功] AI 服务器编译完成
        set /a SUCCESS_COUNT+=1
    )
) else (
    echo [跳过] 找不到 gradlew.bat，跳过 AI 服务器编译
    set /a SKIP_COUNT+=1
)

REM ========================================
REM 3. 编译 AllCommon 模块（必须在 Minecraft 模组之前）
REM ========================================
echo.
echo ========================================
echo [3/4] 编译项目: AllCommon 模块
echo ========================================
cd /d "%ALLCOMMON_PROJECT%"
if exist "build.gradle" (
    REM AllCommon 是独立项目，尝试使用系统 gradle 构建
    REM 如果系统没有 gradle，AllCommon 将在模组编译时自动构建（通过 includeBuild）
    where gradle >nul 2>&1
    if errorlevel 1 (
        echo [提示] 系统未找到 gradle，AllCommon 将在模组编译时自动构建
        set /a SKIP_COUNT+=1
    ) else (
        REM 使用系统 gradle 构建 AllCommon
        gradle build --no-daemon
        if errorlevel 1 (
            echo [失败] AllCommon 模块编译失败
            set /a FAIL_COUNT+=1
        ) else (
            echo [成功] AllCommon 模块编译完成
            set /a SUCCESS_COUNT+=1
        )
    )
) else (
    echo [跳过] 找不到 build.gradle，跳过 AllCommon 模块编译
    set /a SKIP_COUNT+=1
)

REM ========================================
REM 4. 编译 Minecraft 模组项目 (1.20.1)
REM ========================================
echo.
echo ========================================
echo [4/5] 编译项目: Minecraft 模组 (1.20.1)
echo ========================================
cd /d "%MOD_PROJECT_1201%"
if exist "gradlew.bat" (
    REM 编译模组（会自动包含 allcommon，因为使用了 includeBuild）
    REM Gradle 会自动处理 allcommon 的依赖关系和构建
    call gradlew.bat clean build --no-daemon
    if errorlevel 1 (
        echo [失败] Minecraft 模组 1.20.1 编译失败
        set /a FAIL_COUNT+=1
    ) else (
        echo [成功] Minecraft 模组 1.20.1 编译完成（已包含 AllCommon 模块）
        set /a SUCCESS_COUNT+=1
        
        REM 复制模组 JAR 文件到 mods 文件夹
        REM 读取版本号（从 gradle.properties）
        set "MOD_VERSION=1.0"
        set "MC_VERSION=1.20.1"
        if exist "%MOD_PROJECT_1201%\gradle.properties" (
            REM 读取模组版本号
            for /f "tokens=2 delims==" %%a in ('findstr /c:"mod_version=" "%MOD_PROJECT_1201%\gradle.properties"') do (
                set "VERSION_FULL=%%a"
                REM 提取前两部分作为版本号（1.0.0 -> 1.0）
                for /f "tokens=1,2 delims=." %%b in ("!VERSION_FULL!") do (
                    set "MOD_VERSION=%%b.%%c"
                )
            )
            REM 读取 Minecraft 版本号
            for /f "tokens=2 delims==" %%a in ('findstr /c:"minecraft_version=" "%MOD_PROJECT_1201%\gradle.properties"') do (
                set "MC_VERSION=%%a"
            )
        )
        
        REM 创建 mods 文件夹
        set "MODS_DIR=%SCRIPT_DIR%java17(1.20.1-1.20.6)\mods"
        if not exist "%MODS_DIR%" (
            mkdir "%MODS_DIR%"
            echo [创建] mods 文件夹已创建
        )
        
        REM 复制并重命名 JAR 文件
        set "COPIED_COUNT=0"
        
        REM Fabric - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "FABRIC_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1201%\fabric\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "FABRIC_JAR=%MOD_PROJECT_1201%\fabric\build\libs\%%f"
            goto :found_fabric_1201
        )
        :found_fabric_1201
        if defined FABRIC_JAR (
            if exist "!FABRIC_JAR!" (
                copy /Y "!FABRIC_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-fabric.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Fabric: PlayerBot-v%MOD_VERSION%-%MC_VERSION%-fabric.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Fabric JAR 文件失败
                )
            ) else (
                echo [跳过] Fabric JAR 文件不存在
            )
        ) else (
            echo [跳过] Fabric JAR 文件不存在
        )
        
        REM Forge - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "FORGE_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1201%\forge\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "FORGE_JAR=%MOD_PROJECT_1201%\forge\build\libs\%%f"
            goto :found_forge_1201
        )
        :found_forge_1201
        if defined FORGE_JAR (
            if exist "!FORGE_JAR!" (
                copy /Y "!FORGE_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-forge.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Forge:  PlayerBot-v%MOD_VERSION%-%MC_VERSION%-forge.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Forge JAR 文件失败
                )
            ) else (
                echo [跳过] Forge JAR 文件不存在
            )
        ) else (
            echo [跳过] Forge JAR 文件不存在
        )
        
        REM Quilt - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "QUILT_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1201%\quilt\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "QUILT_JAR=%MOD_PROJECT_1201%\quilt\build\libs\%%f"
            goto :found_quilt_1201
        )
        :found_quilt_1201
        if defined QUILT_JAR (
            if exist "!QUILT_JAR!" (
                copy /Y "!QUILT_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-quilt.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Quilt:  PlayerBot-v%MOD_VERSION%-%MC_VERSION%-quilt.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Quilt JAR 文件失败
                )
            ) else (
                echo [跳过] Quilt JAR 文件不存在
            )
        ) else (
            echo [跳过] Quilt JAR 文件不存在
        )
        
        if !COPIED_COUNT! GTR 0 (
            echo [完成] 已复制 !COPIED_COUNT! 个 JAR 文件到 mods 文件夹
        ) else (
            echo [警告] 没有复制任何 JAR 文件
        )
    )
) else (
    echo [跳过] 找不到 gradlew.bat，跳过 Minecraft 模组 1.20.1 编译
    set /a SKIP_COUNT+=1
)

REM ========================================
REM 5. 编译 Minecraft 模组项目 (1.20.4)
REM ========================================
echo.
echo ========================================
echo [5/5] 编译项目: Minecraft 模组 (1.20.4)
echo ========================================
cd /d "%MOD_PROJECT_1204%"
if exist "gradlew.bat" (
    REM 编译模组（会自动包含 allcommon，因为使用了 includeBuild）
    REM Gradle 会自动处理 allcommon 的依赖关系和构建
    call gradlew.bat clean build --no-daemon
    if errorlevel 1 (
        echo [失败] Minecraft 模组 1.20.4 编译失败
        set /a FAIL_COUNT+=1
    ) else (
        echo [成功] Minecraft 模组 1.20.4 编译完成（已包含 AllCommon 模块）
        set /a SUCCESS_COUNT+=1
        
        REM 复制模组 JAR 文件到 mods 文件夹
        REM 读取版本号（从 gradle.properties）
        set "MOD_VERSION=1.0"
        set "MC_VERSION=1.20.4"
        if exist "%MOD_PROJECT_1204%\gradle.properties" (
            REM 读取模组版本号
            for /f "tokens=2 delims==" %%a in ('findstr /c:"mod_version=" "%MOD_PROJECT_1204%\gradle.properties"') do (
                set "VERSION_FULL=%%a"
                REM 提取前两部分作为版本号（1.0.0 -> 1.0）
                for /f "tokens=1,2 delims=." %%b in ("!VERSION_FULL!") do (
                    set "MOD_VERSION=%%b.%%c"
                )
            )
            REM 读取 Minecraft 版本号
            for /f "tokens=2 delims==" %%a in ('findstr /c:"minecraft_version=" "%MOD_PROJECT_1204%\gradle.properties"') do (
                set "MC_VERSION=%%a"
            )
        )
        
        REM 创建 mods 文件夹
        set "MODS_DIR=%SCRIPT_DIR%java17(1.20.1-1.20.6)\mods"
        if not exist "%MODS_DIR%" (
            mkdir "%MODS_DIR%"
            echo [创建] mods 文件夹已创建
        )
        
        REM 复制并重命名 JAR 文件
        set "COPIED_COUNT=0"
        
        REM Fabric - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "FABRIC_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1204%\fabric\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "FABRIC_JAR=%MOD_PROJECT_1204%\fabric\build\libs\%%f"
            goto :found_fabric_1204
        )
        :found_fabric_1204
        if defined FABRIC_JAR (
            if exist "!FABRIC_JAR!" (
                copy /Y "!FABRIC_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-fabric.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Fabric: PlayerBot-v%MOD_VERSION%-%MC_VERSION%-fabric.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Fabric JAR 文件失败
                )
            ) else (
                echo [跳过] Fabric JAR 文件不存在
            )
        ) else (
            echo [跳过] Fabric JAR 文件不存在
        )
        
        REM Forge - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "FORGE_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1204%\forge\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "FORGE_JAR=%MOD_PROJECT_1204%\forge\build\libs\%%f"
            goto :found_forge_1204
        )
        :found_forge_1204
        if defined FORGE_JAR (
            if exist "!FORGE_JAR!" (
                copy /Y "!FORGE_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-forge.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Forge:  PlayerBot-v%MOD_VERSION%-%MC_VERSION%-forge.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Forge JAR 文件失败
                )
            ) else (
                echo [跳过] Forge JAR 文件不存在
            )
        ) else (
            echo [跳过] Forge JAR 文件不存在
        )
        
        REM Quilt - 查找实际的 JAR 文件（排除 dev-shadow 和 sources）
        set "QUILT_JAR="
        for /f "delims=" %%f in ('dir /b "%MOD_PROJECT_1204%\quilt\build\libs\playerbot-*.jar" 2^>nul ^| findstr /v "dev-shadow sources"') do (
            set "QUILT_JAR=%MOD_PROJECT_1204%\quilt\build\libs\%%f"
            goto :found_quilt_1204
        )
        :found_quilt_1204
        if defined QUILT_JAR (
            if exist "!QUILT_JAR!" (
                copy /Y "!QUILT_JAR!" "%MODS_DIR%\PlayerBot-v%MOD_VERSION%-%MC_VERSION%-quilt.jar" >nul
                if not errorlevel 1 (
                    echo [成功] Quilt:  PlayerBot-v%MOD_VERSION%-%MC_VERSION%-quilt.jar
                    set /a COPIED_COUNT+=1
                ) else (
                    echo [失败] 复制 Quilt JAR 文件失败
                )
            ) else (
                echo [跳过] Quilt JAR 文件不存在
            )
        ) else (
            echo [跳过] Quilt JAR 文件不存在
        )
        
        if !COPIED_COUNT! GTR 0 (
            echo [完成] 已复制 !COPIED_COUNT! 个 JAR 文件到 mods 文件夹
        ) else (
            echo [警告] 没有复制任何 JAR 文件
        )
    )
) else (
    echo [跳过] 找不到 gradlew.bat，跳过 Minecraft 模组 1.20.4 编译
    set /a SKIP_COUNT+=1
)

REM ========================================
REM 编译结果汇总
REM ========================================
echo.
echo ========================================
echo   编译完成！
echo ========================================
echo.
echo 编译统计:
echo   成功: %SUCCESS_COUNT% 个项目
echo   失败: %FAIL_COUNT% 个项目
echo   跳过: %SKIP_COUNT% 个项目
echo.

if %FAIL_COUNT% GTR 0 (
    echo ⚠️  有项目编译失败，请检查错误信息
    echo.
) else (
    echo ✅ 所有项目编译成功！
    echo.
)

echo 编译输出位置:
echo   GUI 客户端:     %GUI_PROJECT%\build\libs\
echo   AI 服务器:      %SERVER_PROJECT%\build\libs\
echo   AllCommon 模块: %ALLCOMMON_PROJECT%\build\libs\
echo   1.20.1 Fabric:  %MOD_PROJECT_1201%\fabric\build\libs\
echo   1.20.1 Forge:   %MOD_PROJECT_1201%\forge\build\libs\
echo   1.20.1 Quilt:   %MOD_PROJECT_1201%\quilt\build\libs\
echo   1.20.4 Fabric:  %MOD_PROJECT_1204%\fabric\build\libs\
echo   1.20.4 Forge:   %MOD_PROJECT_1204%\forge\build\libs\
echo   1.20.4 Quilt:   %MOD_PROJECT_1204%\quilt\build\libs\
echo   Mods 文件夹:    %SCRIPT_DIR%java17(1.20.1-1.20.6)\mods\
echo.

REM 显示生成的 JAR 文件
echo 生成的 JAR 文件:
if exist "%GUI_PROJECT%\build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar" (
    echo   ✓ GUI 客户端: PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
)
if exist "%SERVER_PROJECT%\build\libs\PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar" (
    echo   ✓ AI 服务器:  PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar
)
if exist "%ALLCOMMON_PROJECT%\build\libs\playerbot-allcommon-1.0.0.jar" (
    echo   ✓ AllCommon 模块: playerbot-allcommon-1.0.0.jar
)
if exist "%MOD_PROJECT_1201%\fabric\build\libs\*.jar" (
    echo   ✓ 1.20.1 Fabric 模组: fabric\build\libs\*.jar
)
if exist "%MOD_PROJECT_1201%\forge\build\libs\*.jar" (
    echo   ✓ 1.20.1 Forge 模组:  forge\build\libs\*.jar
)
if exist "%MOD_PROJECT_1201%\quilt\build\libs\*.jar" (
    echo   ✓ 1.20.1 Quilt 模组:  quilt\build\libs\*.jar
)
if exist "%MOD_PROJECT_1204%\fabric\build\libs\*.jar" (
    echo   ✓ 1.20.4 Fabric 模组: fabric\build\libs\*.jar
)
if exist "%MOD_PROJECT_1204%\forge\build\libs\*.jar" (
    echo   ✓ 1.20.4 Forge 模组:  forge\build\libs\*.jar
)
if exist "%MOD_PROJECT_1204%\quilt\build\libs\*.jar" (
    echo   ✓ 1.20.4 Quilt 模组:  quilt\build\libs\*.jar
)
echo.

pause

