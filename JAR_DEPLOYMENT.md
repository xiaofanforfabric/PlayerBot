# Fat JAR 部署指南

## 📦 什么是 Fat JAR？

Fat JAR（也称为 Uber JAR 或 Shadow JAR）是一个包含**所有依赖**的 JAR 文件，可以独立运行，无需安装额外的库或配置。

## ✅ 优势

1. **独立运行**：只需 Java 17+，无需 Gradle 或其他工具
2. **易于分发**：单个 JAR 文件即可运行
3. **包含所有依赖**：JavaFX、OkHttp、Gson 等都已打包
4. **跨平台**：Windows、Linux、Mac 都可以运行

## 🚀 使用方法

### 1. 编译生成 Fat JAR

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 2. 运行 Fat JAR

**方法1：使用启动脚本（推荐）**

**Windows:**
```cmd
run-gui-jar.bat
```

**Linux/Mac:**
```bash
chmod +x run-gui-jar.sh
./run-gui-jar.sh
```

**方法2：直接运行**

```bash
java -jar build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
```

## 📁 文件位置

编译后，Fat JAR 文件位于：
```
build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
```

文件大小约 **12MB**，包含：
- ✅ 应用程序代码
- ✅ JavaFX 17.0.2（controls, fxml）
- ✅ OkHttp 4.12.0
- ✅ Gson 2.10.1
- ✅ 所有传递依赖

## 🔄 分发 Fat JAR

### 分发给其他用户

1. **复制 JAR 文件**：将 `PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar` 复制给用户
2. **提供启动脚本**：同时提供 `run-gui-jar.bat`（Windows）或 `run-gui-jar.sh`（Linux/Mac）
3. **说明要求**：告知用户需要 Java 17 或更高版本

### 创建便携版

1. 创建一个文件夹，例如 `FanMacro-GUI-Portable`
2. 将以下文件放入文件夹：
   - `PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar`
   - `run-gui-jar.bat`（Windows）
   - `run-gui-jar.sh`（Linux/Mac）
   - `README.txt`（使用说明）
3. 压缩为 ZIP 文件即可分发

## ⚠️ 注意事项

### Java 版本要求

- **必须使用 Java 17 或更高版本**
- Java 8/11 无法运行（因为使用了 JavaFX 17 和 Java 17 特性）

### 检查 Java 版本

```bash
java -version
```

应该显示：
```
java version "17.0.x" 或更高
```

### 常见问题

**Q: 运行 JAR 时提示 "找不到主清单属性"**
- A: 确保使用的是 `-all.jar` 文件，不是普通的 `.jar` 文件

**Q: 运行 JAR 时提示 JavaFX 相关错误**
- A: 确保使用 Java 17+，Fat JAR 已包含 JavaFX，无需额外配置

**Q: 双击 JAR 文件无法运行**
- A: 使用命令行运行：`java -jar PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar`

**Q: 在不同操作系统上运行**
- A: Fat JAR 是跨平台的，但需要对应平台的 Java 17+。Windows 使用 `.bat` 脚本，Linux/Mac 使用 `.sh` 脚本。

## 🔧 技术细节

### Shadow 插件配置

项目使用 **Shadow Gradle Plugin** 来创建 Fat JAR：

```gradle
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

shadowJar {
    archiveClassifier = 'all'
    // 包含所有运行时依赖
    configurations = [project.configurations.runtimeClasspath]
    // 处理重复文件
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

### 依赖包含

Fat JAR 包含以下依赖：
- `org.openjfx:javafx-controls:17.0.2`
- `org.openjfx:javafx-fxml:17.0.2`
- `com.squareup.okhttp3:okhttp:4.12.0`
- `com.google.code.gson:gson:2.10.1`
- 所有传递依赖（如 OkHttp 的依赖）

## 📊 文件对比

| 文件类型 | 大小 | 包含依赖 | 可独立运行 |
|---------|------|---------|-----------|
| `PlayerBot-Macro-GUI-1.0-SNAPSHOT.jar` | ~30KB | ❌ | ❌ |
| `PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar` | ~12MB | ✅ | ✅ |

**推荐使用 `-all.jar` 文件进行分发和运行。**

