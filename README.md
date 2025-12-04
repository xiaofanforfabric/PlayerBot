# PlayerBot - Minecraft 自动化模组

一个强大的 Minecraft 自动化模组，让你可以通过简单的宏脚本实现复杂的自动化任务。支持 Fabric、Forge 和 Quilt 三个主流加载器，兼容 Minecraft 1.20.1 至 1.20.6。

## 🎮 什么是 PlayerBot？

PlayerBot 是一个自动化辅助模组，它可以帮助你：
- 🤖 执行复杂的自动化任务（挖矿、移动、探索、农场等）
- 📝 使用简单的宏脚本控制游戏
- 🗺️ 自动路径查找和导航
- ⚡ 自动处理游戏事件（死亡、睡眠等）
- 🌐 通过 Web 界面管理宏文件

## ✨ 主要功能

### 宏脚本系统
- 支持自定义宏脚本，实现复杂的自动化流程
- 宏文件保存在 `config/do/` 目录
- 支持条件判断、循环、函数等高级功能
- 实时文件监控，修改后自动重新加载

### 自动化任务
- **路径查找**: 集成 Baritone，智能路径规划
- **自动挖矿**: 自动挖掘指定类型的矿石
- **自动移动**: 精确移动到指定坐标
- **自动探索**: 自动探索周围区域
- **自动农场**: 自动种植和收获作物
- **自动跟随**: 跟随指定实体
- **物品管理**: 检查和管理物品

### 游戏辅助
- **死亡处理**: 玩家死亡时自动停止任务
- **自动睡眠**: 在指定时间自动睡觉
- **HUD 显示**: 显示世界时间等游戏信息
- **命令执行**: 支持执行游戏命令

### Web 管理界面
- 通过 HTTP API 管理宏文件
- 远程控制宏的执行和停止
- 实时查看宏状态

## 📦 安装

### 前置要求

在安装 PlayerBot 之前，你需要：

1. **Minecraft 版本**: 1.20.1、1.20.2、1.20.3、1.20.4、1.20.5 或 1.20.6
2. **Java 版本**: Java 17 或更高版本
3. **模组加载器**: 选择以下之一
   - Fabric Loader
   - Forge
   - Quilt Loader

### 必需依赖模组

PlayerBot 需要以下模组才能正常运行：

#### Baritone API
- **Fabric/Quilt**: 下载 `baritone-api-fabric-1.10.3.jar`
- **Forge**: 下载 `baritone-api-forge-1.10.3.jar`
- 下载地址: [Baritone GitHub Releases](https://github.com/cabaletta/baritone/releases)
- 或通过 Modrinth/CurseForge 搜索 "Baritone API"

#### Architectury API
- 所有平台都需要
- 版本: 9.1.12 或更高
- 下载地址: [Modrinth](https://modrinth.com/mod/architectury-api)

#### 平台特定依赖
- **Fabric**: 需要 Fabric API
- **Forge**: 需要对应版本的 Forge
- **Quilt**: 需要 Quilted Fabric API

### 安装步骤

1. **下载模组文件**
   - 根据你的 Minecraft 版本和加载器，下载对应的 PlayerBot 模组文件
   - 文件名格式: `PlayerBot-v1.0-{MC版本}-{加载器}.jar`
   - 例如: `PlayerBot-v1.0-1.20.1-fabric.jar`

2. **安装依赖模组**
   - 将 Baritone API 和 Architectury API 放入 `mods` 文件夹
   - 安装平台特定的依赖（Fabric API / Forge / Quilted Fabric API）

3. **安装 PlayerBot**
   - 将 PlayerBot 模组文件放入 `mods` 文件夹

4. **启动游戏**
   - 启动 Minecraft，确认所有模组都正确加载

## 🚀 快速开始

### 第一次使用

1. **启动游戏后**，模组会自动创建配置文件目录
2. **宏文件目录**: `.minecraft/config/do/`
3. **配置文件**: `.minecraft/config/playerbot/`

### 创建你的第一个宏

1. 在 `config/do/` 目录创建一个新的文本文件，例如 `my_macro.txt`
2. 编写宏脚本（见下方示例）
3. 保存文件，模组会自动加载
4. 在游戏中执行宏（具体命令见下方）

### 宏脚本示例

#### 示例 1: 移动到指定坐标
```
fan_main:
do #goto 100 64 200;
do end;
```

#### 示例 2: 自动挖矿
```
fan_main:
do #mine diamond_ore;
wait 60s;
do #stop;
do end;
```

#### 示例 3: 条件判断
```
fan_main:
if time >= 18000, do #goto 0 64 0;
if time < 18000, do #mine iron_ore;
do end;
```

#### 示例 4: 检查物品
```
fan_main:
check me have (item = diamond, quantity = 10), do #goto 0 64 0;
check me nothave (item = diamond), do #mine diamond_ore;
do end;
```

## 📖 宏脚本语法

### 基本命令

#### 移动命令
- `do #goto x y z;` - 移动到指定坐标
- `do #stop;` - 停止当前任务

#### 挖矿命令
- `do #mine <矿石类型>;` - 挖掘指定类型的矿石
  - 例如: `do #mine diamond_ore;`
  - 例如: `do #mine iron_ore;`

#### 探索命令
- `do #explore;` - 自动探索周围区域

#### 农场命令
- `do #farm;` - 自动种植和收获作物

#### 跟随命令
- `do #follow <实体名>;` - 跟随指定实体

#### 设置命令
- `do #set allowBreak true/false;` - 允许/禁止破坏方块
- `do #set allowPlace true/false;` - 允许/禁止放置方块（用于控制 Baritone 行为，不是自动建造）

#### 游戏命令
- `do /home;` - 执行原版命令
- `do /spawn;` - 传送到出生点

### 条件语句

#### 位置判断
```
if me at = (x, y, z), do <命令>;
```

#### 时间判断
```
if time = <时间值>, do <命令>;
if time >= <时间值>, do <命令>;
if time <= <时间值>, do <命令>;
```

#### 物品检查
```
check me have (item = <物品>, quantity = <数量>), do <命令>;
check me nothave (item = <物品>), do <命令>;
```

### 控制语句

#### 等待
- `wait Xs;` - 等待 X 秒
- `wait Xm;` - 等待 X 分钟
- `wait Xh;` - 等待 X 小时
- `wait;` - 等待一个游戏刻

#### 结束
- `do end;` - 结束宏执行

#### 运行其他宏
- `run name = "宏名称";` - 运行指定的宏

### 函数定义

#### 定义函数
```
fun name="函数名";
  <函数内容>
```

#### 后台函数
```
fun name="函数名" type= &;
  <函数内容>
```

#### 调用函数
```
do fun "函数名";
```

## ⚙️ 配置

### 配置文件位置

- **宏文件目录**: `.minecraft/config/do/`
- **模组配置**: `.minecraft/config/playerbot/`

### 主要配置项

配置文件中的主要设置：
- Web 服务器端口（用于远程管理）
- 日志级别
- 功能开关

## 🎯 使用技巧

### 最佳实践

1. **测试宏脚本**: 在单人游戏中充分测试宏脚本，确保安全
2. **备份存档**: 使用自动化功能前建议备份存档
3. **遵守规则**: 在多人服务器中使用时，请遵守服务器规则
4. **合理使用**: 不要过度依赖自动化，享受游戏的乐趣

### 常见问题

**Q: 宏文件修改后没有生效？**  
A: 确保文件保存在 `config/do/` 目录，文件扩展名为 `.txt`，模组会自动监控文件变化。

**Q: 如何停止正在运行的宏？**  
A: 使用 `do #stop;` 命令，或在游戏中按相应的停止键（如果配置了）。

**Q: 支持哪些 Minecraft 版本？**  
A: 目前支持 1.20.1 至 1.20.6，未来可能会支持更多版本。

**Q: 可以在服务器中使用吗？**  
A: 可以，但请确保服务器允许使用此类模组，并遵守服务器规则。

## 🔧 故障排除

### 模组无法加载

1. 检查 Minecraft 版本是否匹配
2. 确认所有必需依赖都已安装
3. 检查加载器版本是否兼容
4. 查看游戏日志中的错误信息

### 宏无法执行

1. 检查宏文件语法是否正确
2. 确认宏文件在 `config/do/` 目录
3. 查看游戏日志了解详细错误
4. 确保 Baritone API 已正确安装

### 性能问题

1. 减少同时运行的宏数量
2. 优化宏脚本，避免无限循环
3. 检查是否有其他模组冲突

## 📚 更多资源

- **GitHub 仓库**: [https://github.com/xiaofanforfabric/PlayerBot](https://github.com/xiaofanforfabric/PlayerBot)
- **问题反馈**: 如遇到问题，请在 [GitHub Issues](https://github.com/xiaofanforfabric/PlayerBot/issues) 中反馈
- **功能建议**: 欢迎提出新功能建议
- **社区讨论**: 加入社区讨论，分享使用经验

## ⚠️ 重要提示

- 本模组仅供学习和娱乐使用
- 在多人服务器中使用时，请遵守服务器规则
- 使用自动化功能时请注意游戏平衡性
- 建议在单人游戏或允许使用的服务器中使用

## 📄 许可证

本项目采用 **MIT License with Non-Commercial Use Restriction**（MIT 许可证 + 非商业使用限制）。

**允许：**
- ✅ 个人使用
- ✅ 学习、研究
- ✅ 修改和分发
- ✅ 贡献代码

**禁止：**
- ❌ 商业使用（包括但不限于：销售、用于商业产品、产生收益等）

如需商业使用，请联系作者获取商业许可证。

详见 [LICENSE](LICENSE) 文件。

## 👤 作者

**xiaofan**

---

**享受自动化带来的便利，但不要忘记游戏的本质是乐趣！** 🎮
