package com.xiaofan.version;

import com.xiaofan.api.IGameDirectoryProvider;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * 1.20.1 版本的游戏目录提供者实现
 */
public class GameDirectoryProviderImpl implements IGameDirectoryProvider {
    @Override
    public File getGameDirectory() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return null;
        }
        return mc.gameDirectory;
    }
}

