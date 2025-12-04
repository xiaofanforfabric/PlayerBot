package com.xiaofan.api;

/**
 * 版本提供者
 * 用于获取当前 Minecraft 版本的实现
 * 
 * 各版本需要在初始化时注册自己的实现
 */
public class VersionProvider {
    private static IMinecraftVersion currentVersion;
    
    /**
     * 设置当前版本实现
     * @param version 版本实现
     */
    public static void setVersion(IMinecraftVersion version) {
        currentVersion = version;
    }
    
    /**
     * 获取当前版本实现
     * @return 当前版本实现
     * @throws IllegalStateException 如果版本未设置
     */
    public static IMinecraftVersion getVersion() {
        if (currentVersion == null) {
            throw new IllegalStateException("Minecraft version not initialized. Call VersionProvider.setVersion() first.");
        }
        return currentVersion;
    }
    
    /**
     * 检查版本是否已初始化
     */
    public static boolean isInitialized() {
        return currentVersion != null;
    }
}

