# 迁移总结

## 已完成的工作

### 1. 抽象层接口（`src/main/java/com/xiaofan/api/`）

所有接口已创建，用于抽象 Minecraft API 调用：

- ✅ `IMinecraftVersion` - 主版本接口
- ✅ `IPlayerProvider` - 玩家信息提供者
- ✅ `IWorldTimeProvider` - 世界时间提供者
- ✅ `IItemRegistry` - 物品注册表
- ✅ `ICommandExecutor` - 命令执行器
- ✅ `IPlayerStatusChecker` - 玩家状态检查器
- ✅ `IBlockInteractor` - 方块交互器
- ✅ `IGameDirectoryProvider` - 游戏目录提供者
- ✅ `ILogger` - 日志接口
- ✅ `ITickHandler` - Tick 事件处理器
- ✅ `IBaritoneExecutor` - Baritone 执行器
- ✅ `VersionProvider` - 版本提供者单例
- ✅ `ItemInfo` / `BlockInfo` - 数据类

### 2. 已迁移的核心代码（`src/main/java/com/xiaofan/macro/`）

所有核心逻辑代码已迁移到 `allcommon`，并改为使用抽象接口：

- ✅ `MacroParser` - 宏文件解析器（纯逻辑，无 Minecraft 依赖）
- ✅ `NotFanMacroFound` - 异常类
- ✅ `BaritoneTaskManager` - 任务管理器（已改为使用抽象接口）
- ✅ `MacroExecutor` - 宏执行器（已改为使用抽象接口）
- ✅ `MacroWebServer` - Web 服务器（已改为使用抽象接口）
- ✅ 宏数据结构类（Macro, Function, MacroCommand, DoCommand, WaitCommand, CheckCommand, RunCommand, IfStatement）

## 关键改动

### 所有 Minecraft API 调用已替换为抽象接口：

1. **Minecraft.getInstance()** → `VersionProvider.getVersion()`
2. **mc.gameDirectory** → `version.getGameDirectoryProvider().getGameDirectory()`
3. **mc.player.blockPosition()** → `version.getPlayerProvider().getPlayerPosition()` (返回 `int[]`)
4. **mc.level.getDayTime()** → `version.getWorldTimeProvider().getDayTime()`
5. **mc.player.getInventory()** → `version.getPlayerProvider().getItemInSlot(i)` (返回 `ItemInfo`)
6. **mc.getConnection().sendCommand()** → `version.getCommandExecutor().executeCommand()`
7. **mc.getConnection().sendChat()** → `version.getCommandExecutor().sendChat()`
8. **BuiltInRegistries.ITEM.getKey()** → `version.getItemRegistry().getItemKey()`
9. **Component.literal()** → `version.getPlayerProvider().sendSystemMessage()` (直接传 String)
10. **LogUtils.getLogger()** → `version.getLogger()`
11. **ClientTickHandler.registerClientTick()** → `version.getTickHandler().registerClientTick()`
12. **BaritoneAPI** → `version.getBaritoneExecutor()`

### BlockPos 改为 int[]

所有位置相关的代码都改为使用 `int[]` 数组 `[x, y, z]`，避免直接依赖 `BlockPos` 类。

## 下一步工作

### 1. 在 1.20.1 项目中实现接口

在 `fabric+forge=1.20.1/common/src/main/java/com/xiaofan/version/` 中创建实现类：

- `LoggerImpl` - 实现 ILogger
- `PlayerProviderImpl` - 实现 IPlayerProvider
- `WorldTimeProviderImpl` - 实现 IWorldTimeProvider
- `ItemRegistryImpl` - 实现 IItemRegistry
- `CommandExecutorImpl` - 实现 ICommandExecutor
- `PlayerStatusCheckerImpl` - 实现 IPlayerStatusChecker
- `BlockInteractorImpl` - 实现 IBlockInteractor
- `GameDirectoryProviderImpl` - 实现 IGameDirectoryProvider
- `TickHandlerImpl` - 实现 ITickHandler
- `BaritoneExecutorImpl` - 实现 IBaritoneExecutor
- `MinecraftVersionImpl` - 实现 IMinecraftVersion（主接口）

### 2. 初始化版本提供者

在 `ExampleMod.init()` 中：

```java
MinecraftVersionImpl version = new MinecraftVersionImpl();
VersionProvider.setVersion(version);
```

### 3. 更新依赖

在 `fabric+forge=1.20.1/common/build.gradle` 中添加：

```gradle
dependencies {
    implementation project(':allcommon')
    // 其他依赖...
}
```

在根 `settings.gradle` 中：

```gradle
include("allcommon")
```

### 4. 测试验证

测试所有功能是否正常工作。

## 优势

1. **代码复用**：核心逻辑写一次，所有版本共用
2. **维护简单**：修复 bug 和添加功能只需改一次
3. **版本隔离**：版本差异在实现层处理，不影响核心逻辑
4. **易于扩展**：新增版本只需实现接口即可

## 注意事项

1. `allcommon` 模块**完全不依赖 Minecraft**，只包含纯 Java 逻辑
2. 所有版本特定的对象通过 `Object` 类型传递，在实现层进行类型转换
3. 确保在主线程中调用 Minecraft API（通过 `IPlayerProvider.executeOnMainThread()`）
4. 所有实现方法都应该进行空值检查

