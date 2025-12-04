package com.xiaofan.api;

/**
 * 玩家状态检查器接口
 * 抽象玩家状态相关的检查
 */
public interface IPlayerStatusChecker {
    /**
     * 检查玩家是否死亡或正在死亡
     * @return 是否死亡
     */
    boolean isDeadOrDying();
    
    /**
     * 检查玩家是否在睡觉
     * @return 是否在睡觉
     */
    boolean isSleeping();
    
    /**
     * 检查是否在单人游戏
     * @return 是否在单人游戏
     */
    boolean isSingleplayer();
}

