package com.xiaofan.api;

/**
 * 方块信息数据类
 * 用于在不同版本之间传递方块信息
 */
public class BlockInfo {
    private final Object blockObject;  // 实际的 Block 对象（版本特定）
    private final String blockKey;     // 方块注册表键（如 "minecraft:bed"）
    private final String blockName;    // 方块名称（如 "bed"）
    
    public BlockInfo(Object blockObject, String blockKey, String blockName) {
        this.blockObject = blockObject;
        this.blockKey = blockKey;
        this.blockName = blockName;
    }
    
    /**
     * 获取实际的 Block 对象（版本特定，需要强制转换）
     */
    public Object getBlockObject() {
        return blockObject;
    }
    
    /**
     * 获取方块注册表键
     */
    public String getBlockKey() {
        return blockKey;
    }
    
    /**
     * 获取方块名称
     */
    public String getBlockName() {
        return blockName;
    }
}

