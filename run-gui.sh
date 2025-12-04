#!/bin/bash
# FanMacro GUI 启动脚本 (Linux/Mac)

echo "========================================"
echo "  FanMacro 积木式编程 GUI"
echo "========================================"
echo ""

# 检查 Java 版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请安装 Java 17 或更高版本"
    exit 1
fi

# 运行 GUI
echo "正在启动 GUI..."
echo ""
./gradlew run

if [ $? -ne 0 ]; then
    echo ""
    echo "启动失败，请检查错误信息"
    exit 1
fi

