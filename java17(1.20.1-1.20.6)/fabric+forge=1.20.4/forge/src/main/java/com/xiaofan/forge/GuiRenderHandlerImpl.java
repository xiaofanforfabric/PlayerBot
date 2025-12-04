package com.xiaofan.forge;

import com.xiaofan.GuiRenderHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 平台的 GUI 渲染事件处理器实现
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuiRenderHandlerImpl {
    private static final List<GuiRenderHandler.RenderCallback> renderCallbacks = new ArrayList<>();
    
    /**
     * 注册 GUI 渲染回调
     */
    public static void registerGuiRender(GuiRenderHandler.RenderCallback onRender) {
        synchronized (renderCallbacks) {
            renderCallbacks.add(onRender);
        }
    }
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // 在所有HUD元素渲染完成后渲染自定义内容
        // 使用 CROSSHAIR 层作为触发点（这个层在游戏界面中总是渲染）
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }
        
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return;
        }
        
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        float partialTick = event.getPartialTick();
        net.minecraft.client.gui.GuiGraphics guiGraphics = event.getGuiGraphics();
        
        // 调用所有注册的回调
        synchronized (renderCallbacks) {
            for (GuiRenderHandler.RenderCallback callback : renderCallbacks) {
                try {
                    callback.render(guiGraphics, partialTick, screenWidth, screenHeight);
                } catch (Exception e) {
                    // 记录错误但不中断其他回调
                    e.printStackTrace();
                }
            }
        }
    }
}

