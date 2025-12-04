package com.xiaofan;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * 键盘输入事件平台抽象
 * 用于在多平台（Fabric、Forge、Quilt）上检测键盘按键
 */
public class KeyInputHandler {
    /**
     * 注册键盘按键回调
     * @param keyCode GLFW 按键代码（如 GLFW.GLFW_KEY_HOME）
     * @param onKeyPress 按键按下时的回调（只在按下时调用一次，直到释放后再次按下）
     */
    @ExpectPlatform
    public static void registerKeyPress(int keyCode, Runnable onKeyPress) {
        throw new AssertionError();
    }
    
    /**
     * 检查按键是否当前被按下
     * @param keyCode GLFW 按键代码
     * @return 是否按下
     */
    @ExpectPlatform
    public static boolean isKeyPressed(int keyCode) {
        throw new AssertionError();
    }
}

