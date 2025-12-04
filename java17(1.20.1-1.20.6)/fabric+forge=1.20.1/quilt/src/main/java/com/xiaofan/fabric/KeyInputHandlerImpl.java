package com.xiaofan.fabric;

import com.xiaofan.KeyInputHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quilt 平台的键盘输入事件处理器实现
 * 使用 Fabric API（Quilt 兼容 Fabric）
 * 注意：由于 Quilt 使用 architectury-fabric，所以实现类需要放在 fabric 包下
 */
public class KeyInputHandlerImpl {
    private static final Map<Integer, List<Runnable>> keyPressCallbacks = new HashMap<>();
    private static final Map<Integer, Boolean> lastKeyStates = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * 注册键盘按键回调
     */
    public static void registerKeyPress(int keyCode, Runnable onKeyPress) {
        synchronized (keyPressCallbacks) {
            keyPressCallbacks.computeIfAbsent(keyCode, k -> new ArrayList<>()).add(onKeyPress);
            
            // 首次注册时设置事件监听
            if (!initialized) {
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc != null && mc.getWindow() != null) {
                        long windowHandle = mc.getWindow().getWindow();
                        
                        synchronized (keyPressCallbacks) {
                            for (Map.Entry<Integer, List<Runnable>> entry : keyPressCallbacks.entrySet()) {
                                int currentKeyCode = entry.getKey();
                                boolean isPressed = GLFW.glfwGetKey(windowHandle, currentKeyCode) == GLFW.GLFW_PRESS;
                                Boolean lastState = lastKeyStates.get(currentKeyCode);
                                
                                // 检测按键从释放到按下的变化
                                if (isPressed && (lastState == null || !lastState)) {
                                    // 按键刚被按下，调用所有回调
                                    for (Runnable callback : entry.getValue()) {
                                        try {
                                            callback.run();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                
                                lastKeyStates.put(currentKeyCode, isPressed);
                            }
                        }
                    }
                });
                initialized = true;
            }
        }
    }
    
    /**
     * 检查按键是否当前被按下
     */
    public static boolean isKeyPressed(int keyCode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        long windowHandle = mc.getWindow().getWindow();
        return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
    }
}


