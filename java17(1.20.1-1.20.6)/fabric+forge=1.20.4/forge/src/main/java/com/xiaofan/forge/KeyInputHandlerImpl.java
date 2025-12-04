package com.xiaofan.forge;

import com.xiaofan.KeyInputHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forge 平台的键盘输入事件处理器实现
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyInputHandlerImpl {
    private static final Map<Integer, List<Runnable>> keyPressCallbacks = new HashMap<>();
    private static final Map<Integer, Boolean> lastKeyStates = new HashMap<>();
    
    /**
     * 注册键盘按键回调
     */
    public static void registerKeyPress(int keyCode, Runnable onKeyPress) {
        synchronized (keyPressCallbacks) {
            keyPressCallbacks.computeIfAbsent(keyCode, k -> new ArrayList<>()).add(onKeyPress);
        }
    }
    
    /**
     * 检查按键是否当前被按下
     */
    public static boolean isKeyPressed(int keyCode) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        long windowHandle = mc.getWindow().getWindow();
        return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
    }
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        int keyCode = event.getKey();
        boolean isPressed = event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT;
        
        synchronized (keyPressCallbacks) {
            List<Runnable> callbacks = keyPressCallbacks.get(keyCode);
            if (callbacks != null) {
                Boolean lastState = lastKeyStates.get(keyCode);
                
                // 检测按键从释放到按下的变化
                if (isPressed && (lastState == null || !lastState)) {
                    // 按键刚被按下，调用所有回调
                    for (Runnable callback : callbacks) {
                        try {
                            callback.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                lastKeyStates.put(keyCode, isPressed);
            }
        }
    }
}

