#!/bin/bash
# FanMacro GUI 独立运行脚本 (Linux/Mac)
# 使用包含所有依赖的 Fat JAR，无需 Gradle

echo "========================================"
echo "  FanMacro 积木式编程 GUI (独立运行)"
echo "========================================"
echo ""

# 检查 Java 版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请安装 Java 17 或更高版本"
    exit 1
fi

# 检查 JAR 文件是否存在
if [ ! -f "build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar" ]; then
    echo "错误: 未找到 Fat JAR 文件"
    echo "请先运行: ./gradlew build"
    exit 1
fi

# 运行 Fat JAR（包含所有依赖，可独立运行）
echo "正在启动 GUI（使用 Fat JAR）..."
echo ""
java -jar "build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar"

if [ $? -ne 0 ]; then
    echo ""
    echo "启动失败，请检查错误信息"
    echo ""
    echo "提示: 如果遇到 JavaFX 相关错误，请确保使用 Java 17 或更高版本"
    exit 1
fi

