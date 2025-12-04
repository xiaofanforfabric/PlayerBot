# AllCommon 模块

## 概述

`allcommon` 模块是用于 1.20.1-1.20.6 多版本共用的核心代码模块。通过抽象层设计，将版本特定的 Minecraft API 调用抽象为接口，实现代码复用。

## 架构设计

### 抽象层接口

- `IMinecraftVersion` - 主版本接口，提供所有子接口的访问
- `IPlayerProvider` - 玩家信息提供者
- `IWorldTimeProvider` - 世界时间提供者
- `IItemRegistry` - 物品注册表
- `ICommandExecutor` - 命令执行器
- `IPlayerStatusChecker` - 玩家状态检查器
- `IBlockInteractor` - 方块交互器

### 数据类

- `ItemInfo` - 物品信息
- `BlockInfo` - 方块信息
- `VersionProvider` - 版本提供者（单例）

## 使用方式

### 1. 在各版本项目中实现接口

在 `fabric+forge=1.20.1/common` 等项目中创建实现类：

```java
// 1.20.1 版本的实现
public class MinecraftVersionImpl implements IMinecraftVersion {
    // 实现所有接口方法
}
```

### 2. 初始化版本提供者

在模组初始化时：

```java
// 在 ExampleMod.init() 中
MinecraftVersionImpl version = new MinecraftVersionImpl();
VersionProvider.setVersion(version);
```

### 3. 在核心代码中使用

```java
// 获取版本接口
IMinecraftVersion version = VersionProvider.getVersion();

// 使用抽象接口
int[] pos = version.getPlayerProvider().getPlayerPosition();
long time = version.getWorldTimeProvider().getDayTime();
```

## 目录结构

```
allcommon/
├── build.gradle          # 构建配置（不依赖 Minecraft）
├── settings.gradle       # Gradle 设置
└── src/main/java/
    └── com/xiaofan/
        ├── api/          # 抽象接口
        └── macro/        # 核心逻辑（待迁移）
```

## 迁移进度

### ✅ 已完成

1. ✅ 创建抽象层接口（所有接口已定义）
2. ✅ 迁移 MacroParser（已改为使用抽象接口）
3. ✅ 迁移 NotFanMacroFound（异常类）
4. ✅ 迁移 BaritoneTaskManager（已改为使用抽象接口）
5. ✅ 迁移 MacroExecutor（已改为使用抽象接口）
6. ✅ 迁移 MacroWebServer（已改为使用抽象接口）

### ⏳ 待完成

7. ⏳ 在 1.20.1 项目中创建接口实现类
8. ⏳ 更新 1.20.1 项目以使用 allcommon
9. ⏳ 创建其他版本实现（1.20.2, 1.20.6 等）
10. ⏳ 测试并验证重构后的代码

## 注意事项

- `allcommon` 模块**不依赖 Minecraft**，只包含纯 Java 逻辑
- 版本特定的实现在各版本的 `common` 模块中
- 使用 `VersionProvider` 获取当前版本实现
- 所有版本特定的对象都通过 `Object` 类型传递，在实现层进行类型转换

