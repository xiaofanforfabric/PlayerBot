package com.xiaofan.api;

import java.io.File;

/**
 * 游戏目录提供者接口
 * 用于获取游戏目录路径（用于配置文件等）
 */
public interface IGameDirectoryProvider {
    /**
     * 获取游戏目录
     * @return 游戏目录 File 对象
     */
    File getGameDirectory();
}

