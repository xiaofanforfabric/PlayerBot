package com.xiaofan.version;

import com.xiaofan.api.IWorldTimeProvider;
import net.minecraft.client.Minecraft;

/**
 * 1.20.1 版本的世界时间提供者实现
 */
public class WorldTimeProviderImpl implements IWorldTimeProvider {
    @Override
    public long getWorldTime() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return 0;
        }
        return mc.level.getDayTime();
    }
    
    @Override
    public long getDayTime() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return 0;
        }
        long worldTime = mc.level.getDayTime();
        return worldTime % 24000;
    }
    
    @Override
    public boolean isWorldPresent() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null;
    }
}

