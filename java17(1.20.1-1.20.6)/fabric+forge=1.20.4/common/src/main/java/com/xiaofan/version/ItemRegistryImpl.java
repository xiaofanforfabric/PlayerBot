package com.xiaofan.version;

import com.xiaofan.api.IItemRegistry;
import com.xiaofan.api.ItemInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

/**
 * 1.20.1 版本的物品注册表实现
 */
public class ItemRegistryImpl implements IItemRegistry {
    @Override
    public String getItemKey(ItemInfo itemInfo) {
        if (itemInfo == null || itemInfo.getItemObject() == null) {
            return null;
        }
        try {
            Item item = (Item) itemInfo.getItemObject();
            return BuiltInRegistries.ITEM.getKey(item).toString();
        } catch (ClassCastException e) {
            return itemInfo.getItemKey(); // 如果已经有 itemKey，直接返回
        }
    }
    
    @Override
    public String getItemName(String itemKey) {
        if (itemKey == null || itemKey.isEmpty()) {
            return null;
        }
        int colonIndex = itemKey.indexOf(':');
        if (colonIndex >= 0 && colonIndex < itemKey.length() - 1) {
            return itemKey.substring(colonIndex + 1);
        }
        return itemKey;
    }
    
    @Override
    public boolean isToolItem(ItemInfo itemInfo) {
        if (itemInfo == null || itemInfo.getItemObject() == null) {
            return false;
        }
        try {
            Item item = (Item) itemInfo.getItemObject();
            
            // 检查是否是工具类物品
            if (item instanceof DiggerItem || item instanceof SwordItem) {
                return true;
            }
            
            // 通过物品名称判断
            String itemKey = getItemKey(itemInfo);
            if (itemKey == null) {
                return false;
            }
            String itemName = getItemName(itemKey).toLowerCase();
            
            String[] toolKeywords = {"pickaxe", "axe", "shovel", "hoe", "sword"};
            for (String keyword : toolKeywords) {
                if (itemName.contains(keyword)) {
                    return true;
                }
            }
            
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public String getToolMaterial(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return null;
        }
        
        String itemName = itemInfo.getItemName();
        if (itemName == null) {
            return null;
        }
        
        String itemNameLower = itemName.toLowerCase();
        
        // 常见的工具材质（按优先级）
        String[] materials = {"netherite", "diamond", "golden", "gold", "iron", "stone", "wooden", "wood"};
        
        for (String material : materials) {
            if (itemNameLower.contains(material)) {
                // 标准化材质名称
                if (material.equals("gold")) {
                    return "golden";
                } else if (material.equals("wood")) {
                    return "wooden";
                }
                return material;
            }
        }
        
        return null;
    }
}

