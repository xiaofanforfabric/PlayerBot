# PlayerBot 项目编译说明

## 快速开始

### Windows 用户

#### 方法 1: 使用 PowerShell 脚本（推荐）
```powershell
# 基本编译
.\build-all.ps1

# 清理后编译
.\build-all.ps1 -Clean

# 编译并运行测试
.\build-all.ps1 -Test
```

#### 方法 2: 使用批处理脚本
```cmd
build-all.bat
```

### Linux/Mac 用户

```bash
# 编译主Java项目
./gradlew build

# 编译混合模组项目
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew build
```

## 编译输出位置

编译完成后，模组 JAR 文件位于：

- **Fabric 版本**: `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/fabric/build/libs/playerbot-fabric-1.0.0.jar`
- **Forge 版本**: `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/forge/build/libs/playerbot-forge-1.0.0.jar`
- **Quilt 版本**: `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/quilt/build/libs/playerbot-quilt-1.0.0.jar`

## 单独编译某个平台

### 编译 Fabric 版本
```bash
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew :fabric:build
```

### 编译 Forge 版本
```bash
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew :forge:build
```

### 编译 Quilt 版本
```bash
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew :quilt:build
```

## 开发环境设置

### 前置要求

1. **Java 17** 或更高版本
   - 下载: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 或 [OpenJDK](https://adoptium.net/)
   - 验证: `java -version`

2. **Gradle** (可选，项目包含 Gradle Wrapper)
   - 项目已包含 `gradlew` 和 `gradlew.bat`，无需单独安装

### IDE 设置

#### IntelliJ IDEA
1. 打开项目根目录
2. 等待 Gradle 同步完成
3. 导入项目时选择 "Import Gradle Project"
4. 确保使用 Java 17 SDK

#### Eclipse
1. 安装 Buildship Gradle 插件
2. 导入项目: File → Import → Gradle → Existing Gradle Project
3. 选择项目根目录

#### VS Code
1. 安装 Java Extension Pack
2. 打开项目文件夹
3. 等待 Java 扩展自动识别项目

## 常见问题

### 编译失败：找不到 Baritone API
**解决方案**: 确保 `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/lib/` 目录中存在：
- `baritone-api-fabric-1.10.3.jar`
- `baritone-api-forge-1.10.3.jar`

### 编译失败：内存不足
**解决方案**: 编辑 `gradle.properties`，增加内存分配：
```properties
org.gradle.jvmargs=-Xmx4096M
```

### 编译失败：Gradle 版本不兼容
**解决方案**: 使用项目自带的 Gradle Wrapper，不要使用系统全局的 Gradle

### 编译成功但模组无法加载
**检查清单**:
1. 确保安装了所有运行时依赖（见 `MOD_DEPENDENCIES.md`）
2. 确保 Minecraft 版本为 1.20.1
3. 确保加载器版本兼容
4. 查看游戏日志中的错误信息

## 构建配置

### 修改版本号

编辑 `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/gradle.properties`:
```properties
mod_version=1.0.0
```

### 修改模组 ID

编辑 `java17(1.20.1-1.20.6)/fabric+forge=1.20.1/gradle.properties`:
```properties
archives_base_name=playerbot
```

然后在 `common/src/main/java/com/xiaofan/ExampleMod.java` 中更新:
```java
public static final String MOD_ID = "playerbot";
```

## 发布构建

### 生成发布版本
```bash
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew build --release
```

### 清理构建缓存
```bash
cd "java17(1.20.1-1.20.6)/fabric+forge=1.20.1"
./gradlew clean
```

## 相关文档

- [模组依赖说明](MOD_DEPENDENCIES.md)
- [Architectury 文档](https://docs.architectury.dev/)
- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Forge 文档](https://docs.minecraftforge.net/)

