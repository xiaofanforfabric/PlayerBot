package com.xiaofan.version;

import com.xiaofan.GuiRenderHandler;
import com.xiaofan.api.IGuiRenderHandler;
import com.xiaofan.api.IGuiRenderHandler.IGuiRenderer;
import com.xiaofan.api.IGuiRenderHandler.RenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.20.1 版本的 GUI 渲染处理器实现
 */
public class GuiRenderHandlerImpl implements IGuiRenderHandler {
    private static final List<RenderCallback> renderCallbacks = new ArrayList<>();
    private static boolean initialized = false;
    
    public GuiRenderHandlerImpl() {
        // 首次创建时注册到平台抽象
        if (!initialized) {
            GuiRenderHandler.registerGuiRender((guiGraphics, partialTick, screenWidth, screenHeight) -> {
                // 将 Minecraft 的 GuiGraphics 包装为抽象接口
                IGuiRenderer renderer = new GuiRendererWrapper(guiGraphics);
                synchronized (renderCallbacks) {
                    for (RenderCallback callback : renderCallbacks) {
                        try {
                            callback.render(renderer, partialTick, screenWidth, screenHeight);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            initialized = true;
        }
    }
    
    @Override
    public void registerGuiRender(RenderCallback callback) {
        synchronized (renderCallbacks) {
            renderCallbacks.add(callback);
        }
    }
    
    /**
     * GuiGraphics 包装器，实现 IGuiRenderer 接口
     */
    private static class GuiRendererWrapper implements IGuiRenderer {
        private final GuiGraphics guiGraphics;
        private final Minecraft mc;
        
        public GuiRendererWrapper(GuiGraphics guiGraphics) {
            this.guiGraphics = guiGraphics;
            this.mc = Minecraft.getInstance();
        }
        
        @Override
        public void fill(int x1, int y1, int x2, int y2, int color) {
            guiGraphics.fill(x1, y1, x2, y2, color);
        }
        
        @Override
        public void drawString(String text, int x, int y, int color, boolean shadow) {
            if (mc != null && mc.font != null) {
                guiGraphics.drawString(mc.font, text, x, y, color, shadow);
            }
        }
        
        @Override
        public int getTextWidth(String text) {
            if (mc != null && mc.font != null) {
                return mc.font.width(text);
            }
            return 0;
        }
        
        @Override
        public int getLineHeight() {
            if (mc != null && mc.font != null) {
                return mc.font.lineHeight;
            }
            return 9; // 默认行高
        }
    }
}

