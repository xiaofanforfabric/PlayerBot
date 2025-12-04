# 迁移状态报告

## ✅ 已迁移的类（5个）

1. **MacroParser** - 宏文件解析器 ✅
2. **NotFanMacroFound** - 异常类 ✅
3. **BaritoneTaskManager** - 任务管理器 ✅
4. **MacroExecutor** - 宏执行器 ✅
5. **MacroWebServer** - Web 服务器 ✅

## ⏳ 待迁移的类（3个）

### 1. **DeathHandler** - 死亡处理器
**依赖的 Minecraft API：**
- `Minecraft.getInstance()` → 使用 `VersionProvider`
- `BaritoneAPI` → 使用 `IBaritoneExecutor`
- `Component.literal()` → 使用 `IPlayerProvider.sendSystemMessage()`
- `ClientTickHandler` → 使用 `ITickHandler`

**需要创建的接口：** 无（已有）

### 2. **AutoSleepController** - 自动睡觉控制器
**依赖的 Minecraft API：**
- `Minecraft.getInstance()` → 使用 `VersionProvider`
- `KeyInputHandler` → 需要创建 `IKeyInputHandler` 接口
- `ClientTickHandler` → 使用 `ITickHandler`
- `BlockPos`, `BedBlock`, `InteractionResult` → 使用 `IBlockInteractor`
- `WorldTime` → 使用 `IWorldTimeProvider`

**需要创建的接口：** `IKeyInputHandler`

### 3. **WorldTimeHUD** - 世界时间 HUD
**依赖的 Minecraft API：**
- `Minecraft.getInstance()` → 使用 `VersionProvider`
- `GuiRenderHandler` → 需要创建 `IGuiRenderHandler` 接口
- `GuiGraphics` → 需要抽象 GUI 渲染接口
- `WorldTime` → 使用 `IWorldTimeProvider`

**需要创建的接口：** `IGuiRenderHandler`（包含 GUI 渲染抽象）

## ❌ 不需要迁移的类（6个）

1. **ExampleMod** - 版本特定的入口点，负责初始化
2. **ClientTickHandler** - 平台抽象类（@ExpectPlatform），但需要创建接口供 allcommon 使用
3. **KeyInputHandler** - 平台抽象类（@ExpectPlatform），但需要创建接口供 allcommon 使用
4. **GuiRenderHandler** - 平台抽象类（@ExpectPlatform），但需要创建接口供 allcommon 使用
5. **ExampleExpectPlatform** - 平台抽象类，不需要迁移
6. **MixinTitleScreen** - Mixin 类，版本特定，不需要迁移

## 📋 需要创建的接口（2个）

1. **IKeyInputHandler** - 按键输入接口
   - `registerKeyPress(int keyCode, Runnable onKeyPress)`
   - `isKeyPressed(int keyCode)`

2. **IGuiRenderHandler** - GUI 渲染接口
   - `registerGuiRender(RenderCallback callback)`
   - 需要抽象 `GuiGraphics` 的绘制方法

## 📊 总结

- **已迁移：** 5 个类
- **待迁移：** 3 个类
- **需要创建接口：** 2 个
- **不需要迁移：** 6 个类

**剩余工作量：** 约 3-4 个类需要迁移，2 个接口需要创建

