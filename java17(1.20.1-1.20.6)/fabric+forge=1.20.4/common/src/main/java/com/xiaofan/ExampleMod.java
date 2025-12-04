package com.xiaofan;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.xiaofan.api.VersionProvider;
import com.xiaofan.macro.BaritoneTaskManager;
import com.xiaofan.macro.MacroWebServer;
import com.xiaofan.version.MinecraftVersionImpl;
import com.xiaofan.DeathHandler;
import com.xiaofan.WorldTimeHUD;
import com.xiaofan.AutoSleepController;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class ExampleMod {
    public static final String MOD_ID = "playerbot";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // We can use this if we don't want to use DeferredRegister
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    // Registering a new creative tab
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> EXAMPLE_TAB = TABS.register("example_tab", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + ".example_tab"),
                    () -> new ItemStack(ExampleMod.EXAMPLE_ITEM.get())));
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () ->
            new Item(new Item.Properties().arch$tab(ExampleMod.EXAMPLE_TAB)));
    
    public static void init() {
        TABS.register();
        ITEMS.register();
        
        // 初始化版本提供者（必须在其他模块初始化之前）
        VersionProvider.setVersion(new MinecraftVersionImpl());
        LOGGER.info("版本提供者已初始化: {}", VersionProvider.getVersion().getVersionString());
        
        LOGGER.info("玩家机器人模组已初始化");
        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
        
        // 注册客户端初始化事件
        ClientLifecycleEvent.CLIENT_STARTED.register(client -> {
            onClientInit();
        });
    }
    
    /**
     * 客户端初始化
     * 在客户端启动时初始化所有客户端功能模块
     */
    private static void onClientInit() {
        LOGGER.info("开始初始化客户端功能模块...");
        
        try {
            // 初始化 Baritone 任务管理器（不自动加载宏）
            BaritoneTaskManager.getInstance().initialize();
            LOGGER.info("Baritone 任务管理器已初始化");
            
            // 启动宏管理 Web 服务器（端口 8079）
            MacroWebServer.initialize();
            LOGGER.info("宏管理 Web 服务器已启动，访问 http://localhost:8079 管理宏");
            
            // 初始化死亡处理器
            DeathHandler.initialize();
            LOGGER.info("死亡处理器已初始化");
            
            // 初始化世界时间 HUD
            WorldTimeHUD.initialize();
            LOGGER.info("世界时间 HUD 已初始化");
            
            // 初始化自动睡觉控制器
            AutoSleepController.initialize();
            LOGGER.info("自动睡觉控制器已初始化，按 HOME 键切换开关");
            
            LOGGER.info("所有客户端功能模块初始化完成");
        } catch (Exception e) {
            LOGGER.error("初始化客户端功能模块时出错", e);
        }
    }
}
