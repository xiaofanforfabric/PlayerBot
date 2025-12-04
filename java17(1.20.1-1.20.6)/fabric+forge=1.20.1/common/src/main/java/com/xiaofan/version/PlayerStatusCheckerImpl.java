package com.xiaofan.version;

import com.xiaofan.api.IPlayerStatusChecker;
import net.minecraft.client.Minecraft;

/**
 * 1.20.1 版本的玩家状态检查器实现
 */
public class PlayerStatusCheckerImpl implements IPlayerStatusChecker {
    @Override
    public boolean isDeadOrDying() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return false;
        }
        return mc.player.isDeadOrDying();
    }
    
    @Override
    public boolean isSleeping() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return false;
        }
        return mc.player.isSleeping();
    }
    
    @Override
    public boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSingleplayer();
    }
}

