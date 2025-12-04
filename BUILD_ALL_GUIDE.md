# 一键编译所有项目指南

## 📦 编译脚本

项目提供了两个一键编译脚本：

1. **`build-all.bat`** - Windows 批处理脚本
2. **`build-all.ps1`** - PowerShell 脚本（跨平台）

## 🚀 使用方法

### Windows 批处理脚本

```cmd
build-all.bat
```

### PowerShell 脚本

```powershell
# 基本编译
.\build-all.ps1

# 清理后编译
.\build-all.ps1 -Clean

# 编译并运行测试
.\build-all.ps1 -Test

# 只清理，不编译
.\build-all.ps1 -Clean -Build:$false
```

## 📋 编译的项目

脚本会按顺序编译以下项目：

1. **GUI 客户端** (`PlayerBot-Macro-GUI`)
   - 积木式编程 GUI
   - AI 代码生成客户端
   - 输出：`build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar`

2. **AI 服务器** (`PlayerBot-Macro-GUI-server`)
   - HTTP 服务器
   - 讯飞星火 API 集成
   - 输出：`build/libs/PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar`

3. **Minecraft 模组** (`java17(1.20.1-1.20.6)/fabric+forge=1.20.1`)
   - Fabric 模组
   - Forge 模组
   - Quilt 模组
   - 输出：各平台的 `build/libs/` 目录

## 📊 编译输出

### 成功示例

```
========================================
  编译完成！
========================================

编译统计:
  成功: 3 个项目
  失败: 0 个项目
  跳过: 0 个项目

✅ 所有项目编译成功！

编译输出位置:
  GUI 客户端:     I:\...\build\libs\
  AI 服务器:      I:\...\PlayerBot-Macro-GUI-server\build\libs\
  Fabric 模组:    I:\...\fabric\build\libs\
  Forge 模组:     I:\...\forge\build\libs\
  Quilt 模组:     I:\...\quilt\build\libs\

生成的 JAR 文件:
  ✓ GUI 客户端: PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar
  ✓ AI 服务器:  PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar
  ✓ Fabric 模组: fabric\build\libs\*.jar
  ✓ Forge 模组:  forge\build\libs\*.jar
  ✓ Quilt 模组:  quilt\build\libs\*.jar
```

## ⚙️ 编译选项

### PowerShell 脚本参数

- `-Clean` - 编译前清理构建目录
- `-Build` - 是否编译（默认：`$true`）
- `-Test` - 是否运行测试（默认：`$false`）

### 示例

```powershell
# 清理并编译所有项目
.\build-all.ps1 -Clean

# 只编译，不清理
.\build-all.ps1

# 编译并运行测试
.\build-all.ps1 -Test

# 只清理，不编译
.\build-all.ps1 -Clean -Build:$false
```

## 🔧 故障排除

### 问题1：某个项目编译失败

**解决方案：**
- 查看错误信息，检查依赖是否正确下载
- 检查 Java 版本（需要 Java 17+）
- 尝试单独编译失败的项目

### 问题2：找不到 gradlew.bat

**解决方案：**
- 确保在项目根目录运行脚本
- 检查项目目录结构是否正确

### 问题3：编译时间过长

**解决方案：**
- 这是正常的，特别是第一次编译需要下载依赖
- 可以使用 `--no-daemon` 参数（批处理脚本已包含）

## 📝 注意事项

1. **首次编译**
   - 首次编译会下载所有依赖，可能需要较长时间
   - 确保网络连接正常

2. **Java 版本**
   - 需要 Java 17 或更高版本
   - 检查：`java -version`

3. **磁盘空间**
   - 确保有足够的磁盘空间（至少 1GB）

4. **并行编译**
   - 脚本会按顺序编译，避免资源冲突
   - 如果某个项目失败，会继续编译其他项目

## 🎯 快速开始

```cmd
# Windows
build-all.bat
```

就这么简单！脚本会自动编译所有项目。

## 📂 输出文件位置

编译完成后，JAR 文件位于：

- **GUI 客户端**: `build/libs/PlayerBot-Macro-GUI-1.0-SNAPSHOT-all.jar`
- **AI 服务器**: `PlayerBot-Macro-GUI-server/build/libs/PlayerBot-Macro-GUI-server-1.0-SNAPSHOT-all.jar`
- **Fabric 模组**: `java17(...)/fabric+forge=1.20.1/fabric/build/libs/`
- **Forge 模组**: `java17(...)/fabric+forge=1.20.1/forge/build/libs/`
- **Quilt 模组**: `java17(...)/fabric+forge=1.20.1/quilt/build/libs/`

## 🎉 完成！

一键编译所有项目，就是这么简单！

