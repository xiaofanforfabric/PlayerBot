package com.xiaofan.api;

/**
 * Minecraft 版本抽象接口
 * 用于在不同 Minecraft 版本之间提供统一的 API
 * 
 * 各版本需要实现此接口，提供版本特定的 Minecraft API 调用
 */
public interface IMinecraftVersion {
    /**
     * 获取 Minecraft 主版本号（如 1.20）
     */
    int getMajorVersion();
    
    /**
     * 获取 Minecraft 次版本号（如 1）
     */
    int getMinorVersion();
    
    /**
     * 获取 Minecraft 补丁版本号（如 1.20.1 中的 1）
     */
    int getPatchVersion();
    
    /**
     * 获取版本字符串（如 "1.20.1"）
     */
    String getVersionString();
    
    /**
     * 获取玩家位置提供者
     */
    IPlayerProvider getPlayerProvider();
    
    /**
     * 获取世界时间提供者
     */
    IWorldTimeProvider getWorldTimeProvider();
    
    /**
     * 获取物品注册表
     */
    IItemRegistry getItemRegistry();
    
    /**
     * 获取命令执行器
     */
    ICommandExecutor getCommandExecutor();
    
    /**
     * 获取玩家状态检查器
     */
    IPlayerStatusChecker getPlayerStatusChecker();
    
    /**
     * 获取方块交互器
     */
    IBlockInteractor getBlockInteractor();
    
    /**
     * 获取游戏目录提供者
     */
    IGameDirectoryProvider getGameDirectoryProvider();
    
    /**
     * 获取日志接口
     */
    ILogger getLogger();
    
    /**
     * 获取 Tick 事件处理器
     */
    ITickHandler getTickHandler();
    
    /**
     * 获取 Baritone 执行器
     */
    IBaritoneExecutor getBaritoneExecutor();
    
    /**
     * 获取按键输入处理器
     */
    IKeyInputHandler getKeyInputHandler();
    
    /**
     * 获取 GUI 渲染处理器
     */
    IGuiRenderHandler getGuiRenderHandler();
}

