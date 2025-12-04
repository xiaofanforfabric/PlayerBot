package com.xiaofan;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * 客户端 Tick 事件处理器平台抽象
 * 用于在多平台（Fabric、Forge、Quilt）上注册客户端 tick 事件
 */
public class ClientTickHandler {
    /**
     * 注册客户端 tick 事件监听器
     * 在各个平台实现中注册相应的事件
     */
    @ExpectPlatform
    public static void registerClientTick(Runnable onTick) {
        throw new AssertionError();
    }
}

