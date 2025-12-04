package com.xiaofan.api;

/**
 * 世界时间提供者接口
 * 抽象世界时间相关的操作
 */
public interface IWorldTimeProvider {
    /**
     * 获取当前世界时间（完整时间，包括天数）
     * @return 世界时间（刻），如果世界不存在返回 0
     */
    long getWorldTime();
    
    /**
     * 获取一天内的时间（0-23999 刻）
     * @return 一天内的时间（刻），如果世界不存在返回 0
     */
    long getDayTime();
    
    /**
     * 检查世界是否存在
     */
    boolean isWorldPresent();
}

