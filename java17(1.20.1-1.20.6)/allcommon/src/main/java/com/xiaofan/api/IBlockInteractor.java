package com.xiaofan.api;

/**
 * 方块交互器接口
 * 抽象方块交互相关的操作
 */
public interface IBlockInteractor {
    /**
     * 获取方块状态
     * @param pos 方块位置 [x, y, z]
     * @return 方块信息，如果无法获取返回 null
     */
    BlockInfo getBlockState(int[] pos);
    
    /**
     * 检查方块是否是床
     * @param blockInfo 方块信息
     * @return 是否是床
     */
    boolean isBed(BlockInfo blockInfo);
    
    /**
     * 尝试与方块交互（如点击床）
     * @param pos 方块位置 [x, y, z]
     * @return 交互结果
     */
    InteractionResult interactWithBlock(int[] pos);
    
    /**
     * 交互结果枚举
     */
    enum InteractionResult {
        SUCCESS,    // 成功
        FAILED,     // 失败
        NOT_AVAILABLE  // 不可用（如床被占用）
    }
}

