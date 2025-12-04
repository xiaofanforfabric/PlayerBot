# JavaFX 运行时组件缺失问题 - 解决方案

## 问题描述

运行 Fat JAR 时出现错误：
```
错误: 缺少 JavaFX 运行时组件, 需要使用该组件来运行此应用程序
```

## 原因

JavaFX 17+ 使用了 Java 模块系统，即使将 JavaFX 类打包到 JAR 中，仍然需要：
1. **模块路径** (`--module-path`)：指定 JavaFX 模块的位置
2. **添加模块** (`--add-modules`)：指定需要加载的 JavaFX 模块

## 解决方案

### 方法1：使用自动生成的启动脚本（推荐）

构建项目后，会自动生成 `run-gui-jar.bat` 脚本，该脚本会自动：
1. 从 Gradle 缓存中查找 JavaFX 模块
2. 配置模块路径
3. 启动应用

**使用方法：**
```cmd
run-gui-jar.bat
```

### 方法2：手动指定 JavaFX 模块路径

如果您有 JavaFX SDK，可以手动指定模块路径：

```cmd
java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics -jar build\libs\PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
```

### 方法3：使用 Gradle 运行（最简单）

```cmd
gradlew.bat run
```

## 技术细节

### 构建配置

项目已配置为：
1. **添加平台特定的 JavaFX 依赖**：包含 Windows/Linux/Mac 的本机库
2. **使用 Shadow 插件打包**：将所有依赖打包到 Fat JAR
3. **自动生成启动脚本**：构建后自动生成包含模块路径的启动脚本

### JavaFX 模块路径

启动脚本会自动从 Gradle 缓存中查找 JavaFX 模块：
- `javafx-controls`
- `javafx-fxml`
- `javafx-base`
- `javafx-graphics`

这些模块通常位于：
```
%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.openjfx\
```

### 为什么需要模块路径？

JavaFX 17+ 是模块化的，需要：
1. **模块系统支持**：Java 9+ 的模块系统
2. **本机库加载**：JavaFX 需要加载平台特定的本机库（.dll, .so, .dylib）
3. **模块解析**：Java 需要知道在哪里找到 JavaFX 模块

## 验证

运行启动脚本后，如果成功启动，应该看到：
- GUI 窗口正常显示
- 没有 JavaFX 相关错误

如果仍然失败，请检查：
1. ✅ Java 版本是否为 17 或更高
2. ✅ Gradle 缓存中是否有 JavaFX 模块
3. ✅ JAR 文件是否存在

## 相关文件

- `build.gradle` - 构建配置，包含 JavaFX 依赖和启动脚本生成任务
- `run-gui-jar.bat` - 自动生成的启动脚本（Windows）
- `run-gui-jar.sh` - 启动脚本（Linux/Mac，需要手动创建）

## 更新日志

- **2025-12-02**: 添加自动生成启动脚本功能，自动配置 JavaFX 模块路径

