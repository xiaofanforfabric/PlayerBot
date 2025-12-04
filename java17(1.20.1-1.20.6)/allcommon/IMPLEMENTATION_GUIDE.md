# 实现指南

## 概述

本文档说明如何在各版本项目中实现 `allcommon` 模块定义的抽象接口，以实现 1.20.1-1.20.6 多版本代码复用。

## 实现步骤

### 1. 在版本项目中添加 allcommon 依赖

在 `fabric+forge=1.20.1/common/build.gradle` 中添加：

```gradle
dependencies {
    // 依赖 allcommon 模块
    implementation project(':allcommon')
    
    // 其他依赖...
}
```

在根 `settings.gradle` 中：

```gradle
include("allcommon")
```

### 2. 创建版本实现类

在 `fabric+forge=1.20.1/common/src/main/java/com/xiaofan/version/` 目录下创建实现类：

#### 2.1 实现 ILogger

```java
package com.xiaofan.version;

import com.mojang.logging.LogUtils;
import com.xiaofan.api.ILogger;
import org.slf4j.Logger;

public class LoggerImpl implements ILogger {
    private final Logger logger = LogUtils.getLogger();
    
    @Override
    public void info(String message) {
        logger.info(message);
    }
    
    @Override
    public void info(String format, Object... args) {
        logger.info(format, args);
    }
    
    // 实现其他方法...
}
```

#### 2.2 实现 IPlayerProvider

```java
package com.xiaofan.version;

import com.xiaofan.api.IPlayerProvider;
import com.xiaofan.api.ItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PlayerProviderImpl implements IPlayerProvider {
    @Override
    public int[] getPlayerPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return null;
        }
        BlockPos pos = mc.player.blockPosition();
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }
    
    @Override
    public boolean isPlayerPresent() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null;
    }
    
    @Override
    public int getInventorySize() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return 0;
        }
        return mc.player.getInventory().getContainerSize();
    }
    
    @Override
    public ItemInfo getItemInSlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            return null;
        }
        
        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String itemName = itemKey.substring(itemKey.indexOf(':') + 1);
        
        return new ItemInfo(stack.getItem(), stack.getCount(), itemKey, itemName);
    }
    
    @Override
    public void sendSystemMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("§7[宏] §r" + message));
                }
            });
        }
    }
    
    @Override
    public boolean isOnMainThread() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSameThread();
    }
    
    @Override
    public void executeOnMainThread(Runnable task) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(task);
        }
    }
}
```

#### 2.3 实现其他接口

类似地实现：
- `IWorldTimeProvider`
- `IItemRegistry`
- `ICommandExecutor`
- `IPlayerStatusChecker`
- `IBlockInteractor`
- `IGameDirectoryProvider`

#### 2.4 实现主接口 IMinecraftVersion

```java
package com.xiaofan.version;

import com.xiaofan.api.*;

public class MinecraftVersionImpl implements IMinecraftVersion {
    private final ILogger logger = new LoggerImpl();
    private final IPlayerProvider playerProvider = new PlayerProviderImpl();
    private final IWorldTimeProvider worldTimeProvider = new WorldTimeProviderImpl();
    private final IItemRegistry itemRegistry = new ItemRegistryImpl();
    private final ICommandExecutor commandExecutor = new CommandExecutorImpl();
    private final IPlayerStatusChecker playerStatusChecker = new PlayerStatusCheckerImpl();
    private final IBlockInteractor blockInteractor = new BlockInteractorImpl();
    private final IGameDirectoryProvider gameDirectoryProvider = new GameDirectoryProviderImpl();
    
    @Override
    public int getMajorVersion() {
        return 1;
    }
    
    @Override
    public int getMinorVersion() {
        return 20;
    }
    
    @Override
    public int getPatchVersion() {
        return 1; // 1.20.1
    }
    
    @Override
    public String getVersionString() {
        return "1.20.1";
    }
    
    @Override
    public IPlayerProvider getPlayerProvider() {
        return playerProvider;
    }
    
    @Override
    public IWorldTimeProvider getWorldTimeProvider() {
        return worldTimeProvider;
    }
    
    @Override
    public IItemRegistry getItemRegistry() {
        return itemRegistry;
    }
    
    @Override
    public ICommandExecutor getCommandExecutor() {
        return commandExecutor;
    }
    
    @Override
    public IPlayerStatusChecker getPlayerStatusChecker() {
        return playerStatusChecker;
    }
    
    @Override
    public IBlockInteractor getBlockInteractor() {
        return blockInteractor;
    }
    
    @Override
    public IGameDirectoryProvider getGameDirectoryProvider() {
        return gameDirectoryProvider;
    }
    
    @Override
    public ILogger getLogger() {
        return logger;
    }
}
```

### 3. 初始化版本提供者

在 `ExampleMod.init()` 中：

```java
import com.xiaofan.api.VersionProvider;
import com.xiaofan.version.MinecraftVersionImpl;

public class ExampleMod {
    public static void init() {
        // 初始化版本提供者
        MinecraftVersionImpl version = new MinecraftVersionImpl();
        VersionProvider.setVersion(version);
        
        // 其他初始化代码...
    }
}
```

### 4. 使用 allcommon 中的代码

现在可以在代码中使用 `allcommon` 中的类：

```java
import com.xiaofan.macro.MacroParser;
import com.xiaofan.api.VersionProvider;

// 使用 MacroParser（已迁移到 allcommon）
Macro macro = MacroParser.parse(macroFile);

// 使用版本接口
int[] pos = VersionProvider.getVersion().getPlayerProvider().getPlayerPosition();
```

## 注意事项

1. **版本差异处理**：如果 1.20.1-1.20.6 之间有 API 差异，在实现类中处理
2. **类型转换**：`ItemInfo.getItemObject()` 和 `BlockInfo.getBlockObject()` 返回 `Object`，需要在实现层进行类型转换
3. **线程安全**：确保在主线程中调用 Minecraft API
4. **空值检查**：所有实现方法都应该进行空值检查

## 迁移进度

- ✅ 抽象接口定义
- ✅ MacroParser 迁移
- ✅ NotFanMacroFound 迁移
- ⏳ BaritoneTaskManager 迁移（需要重构）
- ⏳ MacroExecutor 迁移（需要重构）
- ⏳ MacroWebServer 迁移（需要重构）

