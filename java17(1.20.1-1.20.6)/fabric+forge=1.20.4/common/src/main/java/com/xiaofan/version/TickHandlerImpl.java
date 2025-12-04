package com.xiaofan.version;

import com.xiaofan.api.ITickHandler;
import com.xiaofan.ClientTickHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.20.1 版本的 Tick 事件处理器实现
 */
public class TickHandlerImpl implements ITickHandler {
    private static final List<Runnable> tickCallbacks = new ArrayList<>();
    private static boolean initialized = false;
    
    @Override
    public void registerClientTick(Runnable onTick) {
        synchronized (tickCallbacks) {
            tickCallbacks.add(onTick);
            
            // 首次注册时设置事件监听
            if (!initialized) {
                ClientTickHandler.registerClientTick(() -> {
                    synchronized (tickCallbacks) {
                        for (Runnable callback : tickCallbacks) {
                            try {
                                callback.run();
                            } catch (Exception e) {
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

