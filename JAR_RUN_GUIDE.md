# 运行 JAR 文件指南

## ⚠️ 重要提示

由于项目使用了 **JavaFX**，直接运行 JAR 文件需要特殊配置。推荐使用启动脚本或 Gradle 运行。

## 🚀 推荐运行方式

### 方法1：使用启动脚本（最简单）

**Windows:**
```cmd
run-gui.bat
```

**Linux/Mac:**
```bash
./run-gui.sh
```

### 方法2：使用 Gradle（推荐）

```bash
./gradlew run
```

## 📦 运行 JAR 文件

### 如果必须运行 JAR 文件

由于 JavaFX 需要模块路径，直接运行 JAR 可能会失败。如果必须运行 JAR，请使用 **Fat JAR**：

```bash
# 使用 Fat JAR（包含所有依赖）
java -jar build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
```

### JavaFX 模块路径问题

如果遇到 JavaFX 相关错误，需要指定模块路径：

```bash
# Windows
java --module-path "C:\path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml -jar build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar

# Linux/Mac
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
```

## 🔧 故障排除

### 问题1：JAR 文件没有主清单属性

**解决方案：**
- 使用 `-all.jar` 文件（Fat JAR）
- 或使用 `./gradlew run` 运行

### 问题2：JavaFX 相关错误

**错误信息：**
```
Error: JavaFX runtime components are missing
```

**解决方案：**
1. 使用启动脚本（推荐）
2. 使用 `./gradlew run`（推荐）
3. 或配置 JavaFX 模块路径

### 问题3：找不到依赖类

**错误信息：**
```
NoClassDefFoundError: okhttp3/OkHttpClient
```

**解决方案：**
- 使用 `-all.jar` 文件（Fat JAR），它包含了所有依赖

## 📝 文件说明

### JAR 文件类型

1. **`PlayerBot-Macro-GUI-1.0-SNAPSHOT.jar`**
   - 普通 JAR
   - 不包含依赖
   - 不包含 JavaFX
   - ❌ 无法独立运行

2. **`PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar`**
   - Fat JAR
   - 包含所有依赖（OkHttp、Gson）
   - ⚠️ 仍需要 JavaFX 模块路径
   - 可以运行，但需要配置

### 推荐方式

**最佳实践：**
- ✅ 使用 `run-gui.bat` 或 `run-gui.sh`
- ✅ 使用 `./gradlew run`
- ⚠️ 直接运行 JAR 需要额外配置

## 🎯 总结

**最简单的方式：**
```bash
# Windows
run-gui.bat

# Linux/Mac
./run-gui.sh

# 或
./gradlew run
```

这些方式会自动处理所有依赖和 JavaFX 配置！

