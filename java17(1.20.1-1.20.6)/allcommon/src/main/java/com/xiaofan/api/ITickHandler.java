package com.xiaofan.api;

/**
 * Tick 事件处理器接口
 * 用于注册客户端 tick 事件回调
 */
public interface ITickHandler {
    /**
     * 注册客户端 tick 事件监听器
     * @param onTick tick 事件回调
     */
    void registerClientTick(Runnable onTick);
}

