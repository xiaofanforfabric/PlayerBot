package com.xiaofan.version;

import com.xiaofan.KeyInputHandler;
import com.xiaofan.api.IKeyInputHandler;

/**
 * 1.20.1 版本的按键输入处理器实现
 */
public class KeyInputHandlerImpl implements IKeyInputHandler {
    @Override
    public void registerKeyPress(int keyCode, Runnable onKeyPress) {
        KeyInputHandler.registerKeyPress(keyCode, onKeyPress);
    }
    
    @Override
    public boolean isKeyPressed(int keyCode) {
        return KeyInputHandler.isKeyPressed(keyCode);
    }
}

