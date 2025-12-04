package com.xiaofan.api;

/**
 * 玩家信息提供者接口
 * 抽象玩家相关的操作，避免直接依赖 Minecraft 类
 */
public interface IPlayerProvider {
    /**
     * 获取玩家位置（BlockPos 的坐标）
     * @return 玩家位置 [x, y, z]，如果玩家不存在返回 null
     */
    int[] getPlayerPosition();
    
    /**
     * 检查玩家是否存在
     */
    boolean isPlayerPresent();
    
    /**
     * 获取玩家背包大小
     */
    int getInventorySize();
    
    /**
     * 获取指定槽位的物品信息
     * @param slot 槽位索引
     * @return 物品信息，如果槽位为空返回 null
     */
    ItemInfo getItemInSlot(int slot);
    
    /**
     * 发送系统消息到玩家聊天框
     * @param message 消息内容
     */
    void sendSystemMessage(String message);
    
    /**
     * 检查玩家是否在主线程
     */
    boolean isOnMainThread();
    
    /**
     * 在主线程执行任务
     * @param task 要执行的任务
     */
    void executeOnMainThread(Runnable task);
}

