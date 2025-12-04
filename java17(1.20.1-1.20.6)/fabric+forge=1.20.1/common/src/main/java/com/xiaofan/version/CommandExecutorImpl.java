package com.xiaofan.version;

import com.xiaofan.api.ICommandExecutor;
import net.minecraft.client.Minecraft;

/**
 * 1.20.1 版本的命令执行器实现
 */
public class CommandExecutorImpl implements ICommandExecutor {
    @Override
    public boolean executeCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null || mc.player == null) {
            return false;
        }
        
        try {
            // sendCommand 不需要 / 前缀
            mc.getConnection().sendCommand(command);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean sendChat(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null || mc.player == null) {
            return false;
        }
        
        try {
            mc.getConnection().sendChat(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isConnected() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.getConnection() != null;
    }
}

