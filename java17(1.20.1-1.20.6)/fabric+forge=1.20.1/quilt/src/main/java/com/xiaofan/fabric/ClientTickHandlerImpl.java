package com.xiaofan.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Quilt 平台的客户端 Tick 事件处理器实现
 * Quilt 兼容 Fabric API，所以使用相同的实现
 * 注意：由于 Quilt 使用 architectury-fabric，所以实现类需要放在 fabric 包下
 */
public class ClientTickHandlerImpl {
    private static final List<Runnable> tickCallbacks = new ArrayList<>();
    private static boolean initialized = false;
    
    /**
     * 注册客户端 tick 回调
     */
    public static void registerClientTick(Runnable onTick) {
        synchronized (tickCallbacks) {
            tickCallbacks.add(onTick);
            
            // 首次注册时设置事件监听
            if (!initialized) {
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
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
                });
                initialized = true;
            }
        }
    }
}


