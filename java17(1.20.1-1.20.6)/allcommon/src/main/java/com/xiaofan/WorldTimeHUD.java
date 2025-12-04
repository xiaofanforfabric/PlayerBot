package com.xiaofan;

import com.xiaofan.api.*;

/**
 * 世界时间 HUD 显示
 * 在屏幕顶部中间显示服务器世界时间
 */
public class WorldTimeHUD {
    private static boolean initialized = false;
    
    /**
     * 初始化世界时间 HUD
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        // 使用抽象接口注册 GUI 渲染事件
        version.getGuiRenderHandler().registerGuiRender(WorldTimeHUD::onRenderGui);
        initialized = true;
        version.getLogger().info("[世界时间HUD] 世界时间 HUD 已初始化");
    }
    
    /**
     * GUI 渲染事件处理
     */
    private static void onRenderGui(IGuiRenderHandler.IGuiRenderer renderer, float partialTick, int screenWidth, int screenHeight) {
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        IPlayerProvider playerProvider = version.getPlayerProvider();
        IWorldTimeProvider worldTimeProvider = version.getWorldTimeProvider();
        
        if (!playerProvider.isPlayerPresent() || !worldTimeProvider.isWorldPresent()) {
            return;
        }
        
        // 获取服务器世界时间（不是客户端本地时间）
        // getDayTime() 返回的是服务器同步的世界时间
        long dayTime = worldTimeProvider.getDayTime();
        
        // 计算小时和分钟
        // Minecraft 一天 = 24000 刻
        // 游戏内时间从 0 刻（6:00）开始
        // 游戏内时间：0刻 = 6:00, 1000刻 = 7:00, 6000刻 = 12:00, 18000刻 = 18:00
        int hours = (int) ((dayTime / 1000 + 6) % 24);
        int minutes = (int) ((dayTime % 1000) * 60 / 1000);
        
        // 格式化时间显示：00:00，xx刻
        String timeString = String.format("%02d:%02d", hours, minutes);
        String tickString = String.format("%d刻", dayTime);
        String displayText = timeString + "，" + tickString;
        
        // 计算文本宽度以居中显示
        int textWidth = renderer.getTextWidth(displayText);
        int x = (screenWidth - textWidth) / 2;
        int y = 10; // 距离顶部10像素
        
        // 绘制半透明背景
        int padding = 4;
        int bgColor = 0x80000000; // 半透明黑色 (ARGB: 80 = 50% 透明度)
        renderer.fill(x - padding, y - padding, x + textWidth + padding, y + renderer.getLineHeight() + padding, bgColor);
        
        // 绘制文本（白色）
        renderer.drawString(displayText, x, y, 0xFFFFFF, false);
    }
}

