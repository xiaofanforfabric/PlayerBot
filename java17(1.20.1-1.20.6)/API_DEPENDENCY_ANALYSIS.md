# API 依赖分析报告

## 三个待迁移类的 API 依赖情况

### 1. DeathHandler - ❌ 直接依赖 Minecraft API

**直接依赖的 API：**
- ✅ `Minecraft.getInstance()` - 直接调用（需要抽象）
- ✅ `BaritoneAPI.getProvider().getPrimaryBaritone()` - 直接调用（需要抽象）
- ✅ `Component.literal()` - 直接调用（需要抽象）
- ✅ `mc.player.sendSystemMessage()` - 直接调用（需要抽象）
- ✅ `mc.getConnection().sendChat()` - 直接调用（需要抽象）
- ✅ `mc.isSingleplayer()` - 直接调用（需要抽象）
- ✅ `mc.player.isDeadOrDying()` - 直接调用（需要抽象）
- ✅ `ClientTickHandler` - 平台抽象，但需要改为使用 `ITickHandler`

**可用的抽象接口：**
- ✅ `IPlayerStatusChecker.isDeadOrDying()` - 已有
- ✅ `IPlayerStatusChecker.isSingleplayer()` - 已有
- ✅ `IPlayerProvider.sendSystemMessage()` - 已有
- ✅ `IBaritoneExecutor.executeCommand()` - 已有
- ✅ `ITickHandler.registerClientTick()` - 已有
- ✅ `ILogger` - 已有

**结论：** 需要迁移，大部分接口已存在，只需替换调用

---

### 2. AutoSleepController - ❌ 直接依赖 Minecraft API

**直接依赖的 API：**
- ✅ `Minecraft.getInstance()` - 直接调用（需要抽象）
- ✅ `BlockPos` - 直接使用 Minecraft 类型（需要抽象为 `int[]`）
- ✅ `BedBlock` - 直接使用 Minecraft 类型（需要抽象）
- ✅ `BlockState`, `Block` - 直接使用 Minecraft 类型（需要抽象）
- ✅ `InteractionResult` - 直接使用 Minecraft 类型（已有抽象枚举）
- ✅ `Vec3` - 直接使用 Minecraft 类型（需要抽象为 `double[]` 或计算）
- ✅ `LocalPlayer`, `Player` - 直接使用 Minecraft 类型（需要抽象）
- ✅ `mc.level.getDayTime()` - 直接调用（需要抽象）
- ✅ `mc.player.isSleeping()` - 直接调用（需要抽象）
- ✅ `KeyInputHandler` - 平台抽象，但需要接口
- ✅ `ClientTickHandler` - 平台抽象，但需要改为使用 `ITickHandler`

**可用的抽象接口：**
- ✅ `IWorldTimeProvider.getDayTime()` - 已有
- ✅ `IPlayerStatusChecker.isSleeping()` - 已有
- ✅ `IBlockInteractor.getBlockState()` - 已有（返回 `BlockInfo`）
- ✅ `IBlockInteractor.isBed()` - 已有
- ✅ `IBlockInteractor.interactWithBlock()` - 已有
- ✅ `IPlayerProvider.getPlayerPosition()` - 已有（返回 `int[]`）
- ✅ `ITickHandler.registerClientTick()` - 已有
- ✅ `ILogger` - 已有

**需要创建的接口：**
- ❌ `IKeyInputHandler` - 需要创建

**结论：** 需要迁移，大部分接口已存在，但需要：
1. 创建 `IKeyInputHandler` 接口
2. 将 `BlockPos` 相关逻辑改为使用 `int[]` 和 `IBlockInteractor`
3. 将 `Vec3` 距离计算改为使用 `int[]` 位置计算

---

### 3. WorldTimeHUD - ❌ 直接依赖 Minecraft API

**直接依赖的 API：**
- ✅ `Minecraft.getInstance()` - 直接调用（需要抽象）
- ✅ `GuiGraphics` - 直接使用 Minecraft 类型（需要抽象）
- ✅ `mc.font` - 直接使用 Minecraft 类型（需要抽象）
- ✅ `mc.level.getDayTime()` - 直接调用（需要抽象）
- ✅ `mc.screen` - 直接调用（需要抽象）
- ✅ `GuiRenderHandler` - 平台抽象，但需要接口

**可用的抽象接口：**
- ✅ `IWorldTimeProvider.getDayTime()` - 已有
- ✅ `ILogger` - 已有

**需要创建的接口：**
- ❌ `IGuiRenderHandler` - 需要创建（包含 GUI 渲染抽象）

**结论：** 需要迁移，但需要：
1. 创建 `IGuiRenderHandler` 接口
2. 抽象 `GuiGraphics` 的绘制方法（`fill`, `drawString`）
3. 抽象字体相关操作（`font.width()`, `font.lineHeight`）

---

## 总结

| 类名 | 直接依赖 API | 已有接口 | 需要创建接口 | 迁移难度 |
|------|------------|---------|------------|---------|
| **DeathHandler** | ✅ 是 | ✅ 大部分已有 | ❌ 无 | 🟢 简单 |
| **AutoSleepController** | ✅ 是 | ✅ 大部分已有 | ✅ `IKeyInputHandler` | 🟡 中等 |
| **WorldTimeHUD** | ✅ 是 | ⚠️ 部分已有 | ✅ `IGuiRenderHandler` | 🟡 中等 |

**结论：** 这三个类都**直接依赖 Minecraft API**，需要迁移并抽象化。

