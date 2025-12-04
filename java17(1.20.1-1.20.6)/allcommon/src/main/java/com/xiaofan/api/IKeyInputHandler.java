package com.xiaofan.api;

/**
 * 按键输入处理器接口
 * 用于抽象键盘按键输入相关的操作
 */
public interface IKeyInputHandler {
    /**
     * 注册键盘按键回调
     * @param keyCode GLFW 按键代码（如 GLFW.GLFW_KEY_HOME）
     * @param onKeyPress 按键按下时的回调（只在按下时调用一次，直到释放后再次按下）
     */
    void registerKeyPress(int keyCode, Runnable onKeyPress);
    
    /**
     * 检查按键是否当前被按下
     * @param keyCode GLFW 按键代码
     * @return 是否按下
     */
    boolean isKeyPressed(int keyCode);
}

