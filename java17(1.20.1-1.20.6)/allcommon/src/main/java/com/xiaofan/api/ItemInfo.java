package com.xiaofan.api;

/**
 * 物品信息数据类
 * 用于在不同版本之间传递物品信息
 */
public class ItemInfo {
    private final Object itemObject;  // 实际的 Item 对象（版本特定）
    private final int count;           // 物品数量
    private final String itemKey;      // 物品注册表键（如 "minecraft:diamond_pickaxe"）
    private final String itemName;      // 物品名称（如 "diamond_pickaxe"）
    
    public ItemInfo(Object itemObject, int count, String itemKey, String itemName) {
        this.itemObject = itemObject;
        this.count = count;
        this.itemKey = itemKey;
        this.itemName = itemName;
    }
    
    /**
     * 获取实际的 Item 对象（版本特定，需要强制转换）
     */
    public Object getItemObject() {
        return itemObject;
    }
    
    /**
     * 获取物品数量
     */
    public int getCount() {
        return count;
    }
    
    /**
     * 获取物品注册表键
     */
    public String getItemKey() {
        return itemKey;
    }
    
    /**
     * 获取物品名称
     */
    public String getItemName() {
        return itemName;
    }
    
    /**
     * 检查物品是否为空
     */
    public boolean isEmpty() {
        return count <= 0 || itemObject == null;
    }
}

