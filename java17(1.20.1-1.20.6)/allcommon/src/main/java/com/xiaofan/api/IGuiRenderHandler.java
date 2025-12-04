package com.xiaofan.api;

/**
 * GUI 渲染处理器接口
 * 用于在游戏界面上渲染自定义内容
 */
public interface IGuiRenderHandler {
    /**
     * 注册 GUI 渲染回调
     * @param callback 渲染回调
     */
    void registerGuiRender(RenderCallback callback);
    
    /**
     * GUI 渲染回调接口
     */
    @FunctionalInterface
    interface RenderCallback {
        /**
         * 渲染回调
         * @param renderer GUI 渲染器（抽象了 GuiGraphics）
         * @param partialTick 部分 tick（用于平滑动画）
         * @param screenWidth 屏幕宽度
         * @param screenHeight 屏幕高度
         */
        void render(IGuiRenderer renderer, float partialTick, int screenWidth, int screenHeight);
    }
    
    /**
     * GUI 渲染器接口（抽象 GuiGraphics）
     */
    interface IGuiRenderer {
        /**
         * 绘制填充矩形（用于背景）
         * @param x1 左上角 X 坐标
         * @param y1 左上角 Y 坐标
         * @param x2 右下角 X 坐标
         * @param y2 右下角 Y 坐标
         * @param color ARGB 颜色值
         */
        void fill(int x1, int y1, int x2, int y2, int color);
        
        /**
         * 绘制文本
         * @param text 文本内容
         * @param x X 坐标
         * @param y Y 坐标
         * @param color ARGB 颜色值
         * @param shadow 是否绘制阴影
         */
        void drawString(String text, int x, int y, int color, boolean shadow);
        
        /**
         * 获取文本宽度
         * @param text 文本内容
         * @return 文本宽度（像素）
         */
        int getTextWidth(String text);
        
        /**
         * 获取字体行高
         * @return 行高（像素）
         */
        int getLineHeight();
    }
}

