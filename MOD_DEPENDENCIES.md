# PlayerBot 模组依赖说明

## 1.20.1 混合模组项目运行时依赖

### 必需依赖（必须安装）

#### 1. **Baritone API 模组**
- **说明**: 本模组依赖 Baritone API 模组来执行自动化任务。Baritone API 是官方发布的完整模组版本。
- **重要**: Baritone API 的模组 ID 在不同平台不同：
  - **Forge 版本**: `baritoe`（注意拼写，不是 `baritone`）
  - **Fabric/Quilt 版本**: `baritone`
  
- **Fabric/Quilt 版本**: 
  - 模组名称: `Baritone`
  - 模组 ID: `baritone`
  - 版本: `1.10.3` 或兼容版本
  - 文件名: `baritone-api-fabric-1.10.3.jar`
  - 下载: [Baritone GitHub Releases](https://github.com/cabaletta/baritone/releases)
  - 或通过 Modrinth/CurseForge 搜索 "Baritone API"
  
- **Forge 版本**:
  - 模组名称: `Baritone`
  - 模组 ID: `baritoe`（注意：这是模组配置中的实际 ID，可能是拼写错误但需要匹配）
  - 版本: `1.10.3` 或兼容版本
  - 文件名: `baritone-api-forge-1.10.3.jar`
  - 下载: [Baritone GitHub Releases](https://github.com/cabaletta/baritone/releases)
  - 或通过 Modrinth/CurseForge 搜索 "Baritone API"

#### 2. **Architectury API**
- **说明**: 多平台模组框架，本模组基于此框架开发
- **Fabric 版本**:
  - 模组名称: `Architectury API`
  - 版本: `9.1.12` (与 `gradle.properties` 中的版本一致)
  - 下载: [Architectury Modrinth](https://modrinth.com/mod/architectury-api)
  
- **Forge 版本**:
  - 模组名称: `Architectury API`
  - 版本: `9.1.12`
  - 下载: [Architectury Modrinth](https://modrinth.com/mod/architectury-api)
  
- **Quilt 版本**:
  - 模组名称: `Architectury API`
  - 版本: `9.1.12`
  - 下载: [Architectury Modrinth](https://modrinth.com/mod/architectury-api)

### 平台特定依赖

#### Fabric 平台
- **Fabric API**
  - 版本: `0.90.4+1.20.1` (与 `gradle.properties` 中的版本一致)
  - 下载: [Fabric API Modrinth](https://modrinth.com/mod/fabric-api)
  - 说明: Fabric 模组开发的基础 API

#### Forge 平台
- **Forge**
  - 版本: `1.20.1-47.2.1` (与 `gradle.properties` 中的版本一致)
  - 说明: Forge 加载器本身，不是额外模组

#### Quilt 平台
- **Quilted Fabric API**
  - 版本: `7.4.0+0.90.0-1.20.1` (与 `gradle.properties` 中的版本一致)
  - 下载: [Quilted Fabric API Modrinth](https://modrinth.com/mod/qsl)
  - 说明: Quilt 版本的 Fabric API 兼容层

### 依赖关系图

```
PlayerBot 模组
├── Baritone API 模组 (必需)
│   └── 提供自动化路径查找和执行功能
│   └── 模组 ID: 
│       ├── Forge: baritoe (注意拼写)
│       └── Fabric/Quilt: baritone
│
├── Architectury API (必需)
│   └── 提供多平台抽象层
│
└── 平台特定依赖
    ├── Fabric: Fabric API
    ├── Forge: Forge 加载器
    └── Quilt: Quilted Fabric API
```

### 安装步骤

#### 方法 1: 手动安装
1. 下载所有必需依赖模组
2. 将模组文件放入 Minecraft 的 `mods` 文件夹
3. 启动游戏

#### 方法 2: 使用模组管理器（推荐）
- **Modrinth App**: [Modrinth App](https://modrinth.com/app)
- **CurseForge App**: [CurseForge App](https://www.curseforge.com/download/app)
- **MultiMC/Prism Launcher**: 支持自动依赖管理

### 版本兼容性

- **Minecraft 版本**: `1.20.1`
- **Fabric Loader**: `0.14.23` 或更高
- **Forge**: `47.2.1` 或兼容版本
- **Quilt Loader**: `0.21.2-beta.2` 或更高

### 故障排除

#### 问题: 模组无法加载
- **检查**: 确保所有必需依赖都已安装
- **检查**: 确保依赖版本与模组兼容
- **检查**: 查看游戏日志中的错误信息

#### 问题: Baritone 功能不可用
- **检查**: 确保 Baritone API 模组已正确安装（文件名应为 `baritone-api-forge-1.10.3.jar` 或 `baritone-api-fabric-1.10.3.jar`）
- **检查**: 确保 Baritone API 版本与 Minecraft 1.20.1 兼容
- **检查**: 确保模组 ID 正确：
  - Forge 版本: `baritoe`（注意拼写）
  - Fabric/Quilt 版本: `baritone`
- **检查**: 查看游戏日志中是否有 Baritone 相关错误

#### 问题: Fabric 版本模组名称显示乱码
- **原因**: JSON 文件编码问题
- **解决**: 已修复 `build.gradle` 中的编码设置，重新编译即可
- **检查**: 确保 `fabric/build.gradle` 中的 `processResources` 包含 `filteringCharset = "UTF-8"`

#### 问题: 平台特定错误
- **Fabric**: 确保安装了 Fabric API
- **Forge**: 确保 Forge 版本正确
- **Quilt**: 确保安装了 Quilted Fabric API

### 开发依赖（仅编译时需要）

以下依赖已在 `build.gradle` 中配置，编译时会自动下载：

- **Baritone API** (已在 `lib` 目录中)
  - `baritone-api-fabric-1.10.3.jar`
  - `baritone-api-forge-1.10.3.jar`
  
- **Architectury Loom** (Gradle 插件)
- **Fabric Loom** (Gradle 插件)
- **Shadow Plugin** (用于打包)

### 更新依赖版本

如果需要更新依赖版本，请修改以下文件：

1. **`gradle.properties`**: 更新模组版本号
2. **`build.gradle`**: 更新 Gradle 插件版本
3. **`lib/` 目录**: 更新 Baritone API JAR 文件

### 相关链接

- [Baritone GitHub](https://github.com/cabaletta/baritone)
- [Architectury 文档](https://docs.architectury.dev/)
- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Forge 文档](https://docs.minecraftforge.net/)
- [Quilt 文档](https://quiltmc.org/)

