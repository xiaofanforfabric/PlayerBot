package com.xiaofan;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * GUI 渲染事件平台抽象
 * 用于在游戏界面上渲染自定义内容
 */
public class GuiRenderHandler {
    /**
     * 注册 GUI 渲染回调
     * @param onRender 渲染回调，参数为 (GuiGraphics, float partialTick, screenWidth, screenHeight)
     */
    @ExpectPlatform
    public static void registerGuiRender(RenderCallback onRender) {
        throw new AssertionError();
    }
    
    /**
     * GUI 渲染回调接口
     */
    @FunctionalInterface
    public interface RenderCallback {
        /**
         * 渲染回调
         * @param guiGraphics GUI 图形上下文
         * @param partialTick 部分 tick（用于平滑动画）
         * @param screenWidth 屏幕宽度
         * @param screenHeight 屏幕高度
         */
        void render(net.minecraft.client.gui.GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);
    }
}

