package com.xiaofan.fabric;

import com.xiaofan.GuiRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric 平台的 GUI 渲染事件处理器实现
 */
public class GuiRenderHandlerImpl {
    private static final List<GuiRenderHandler.RenderCallback> renderCallbacks = new ArrayList<>();
    private static boolean initialized = false;
    
    /**
     * 注册 GUI 渲染回调
     */
    public static void registerGuiRender(GuiRenderHandler.RenderCallback onRender) {
        synchronized (renderCallbacks) {
            renderCallbacks.add(onRender);
            
            // 首次注册时设置事件监听
            if (!initialized) {
                HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc != null && mc.getWindow() != null) {
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        
                        // 调用所有注册的回调
                        synchronized (renderCallbacks) {
                            for (GuiRenderHandler.RenderCallback callback : renderCallbacks) {
                                try {
                                    callback.render(guiGraphics, tickDelta, screenWidth, screenHeight);
                                } catch (Exception e) {
                                    // 记录错误但不中断其他回调
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                initialized = true;
            }
        }
    }
}

