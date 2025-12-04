package com.xiaofan.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 平台的客户端 Tick 事件处理器实现
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTickHandlerImpl {
    private static final List<Runnable> tickCallbacks = new ArrayList<>();
    
    /**
     * 注册客户端 tick 回调
     */
    public static void registerClientTick(Runnable onTick) {
        synchronized (tickCallbacks) {
            tickCallbacks.add(onTick);
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        // 调用所有注册的回调
        synchronized (tickCallbacks) {
            for (Runnable callback : tickCallbacks) {
                try {
                    callback.run();
                } catch (Exception e) {
                    // 记录错误但不中断其他回调
                    e.printStackTrace();
                }
            }
        }
    }
}

