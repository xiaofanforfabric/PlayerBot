package com.xiaofan.version;

import com.xiaofan.api.IPlayerProvider;
import com.xiaofan.api.ItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 1.20.1 版本的玩家提供者实现
 */
public class PlayerProviderImpl implements IPlayerProvider {
    @Override
    public int[] getPlayerPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return null;
        }
        BlockPos pos = mc.player.blockPosition();
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }
    
    @Override
    public boolean isPlayerPresent() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null;
    }
    
    @Override
    public int getInventorySize() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return 0;
        }
        return mc.player.getInventory().getContainerSize();
    }
    
    @Override
    public ItemInfo getItemInSlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            return null;
        }
        
        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String itemName = itemKey.substring(itemKey.indexOf(':') + 1);
        
        return new ItemInfo(stack.getItem(), stack.getCount(), itemKey, itemName);
    }
    
    @Override
    public void sendSystemMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendSystemMessage(Component.literal("§7[宏] §r" + message));
                }
            });
        }
    }
    
    @Override
    public boolean isOnMainThread() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSameThread();
    }
    
    @Override
    public void executeOnMainThread(Runnable task) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(task);
        }
    }
}

