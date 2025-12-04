package com.xiaofan.api;

/**
 * 物品注册表接口
 * 抽象物品相关的操作
 */
public interface IItemRegistry {
    /**
     * 获取物品的注册表键（如 "minecraft:diamond_pickaxe"）
     * @param itemInfo 物品信息
     * @return 物品注册表键，如果无法获取返回 null
     */
    String getItemKey(ItemInfo itemInfo);
    
    /**
     * 从注册表键获取物品名称（去掉命名空间，如 "diamond_pickaxe"）
     * @param itemKey 物品注册表键（如 "minecraft:diamond_pickaxe"）
     * @return 物品名称（如 "diamond_pickaxe"），如果无法解析返回 null
     */
    String getItemName(String itemKey);
    
    /**
     * 检查物品是否是工具类物品（镐、斧、铲、锄、剑等）
     * @param itemInfo 物品信息
     * @return 是否是工具类物品
     */
    boolean isToolItem(ItemInfo itemInfo);
    
    /**
     * 获取工具材质（如 "diamond", "iron", "netherite"）
     * @param itemInfo 物品信息
     * @return 工具材质，如果不是工具或无法识别返回 null
     */
    String getToolMaterial(ItemInfo itemInfo);
}

