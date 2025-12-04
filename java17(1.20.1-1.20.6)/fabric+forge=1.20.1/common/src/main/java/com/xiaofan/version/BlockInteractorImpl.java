package com.xiaofan.version;

import com.xiaofan.api.BlockInfo;
import com.xiaofan.api.IBlockInteractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
// 不导入 InteractionResult，使用完全限定名避免与 IBlockInteractor.InteractionResult 冲突
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.20.1 版本的方块交互器实现
 */
public class BlockInteractorImpl implements IBlockInteractor {
    @Override
    public BlockInfo getBlockState(int[] pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || pos == null || pos.length < 3) {
            return null;
        }
        
        BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
        BlockState state = mc.level.getBlockState(blockPos);
        Block block = state.getBlock();
        
        String blockKey = BuiltInRegistries.BLOCK.getKey(block).toString();
        String blockName = blockKey.substring(blockKey.indexOf(':') + 1);
        
        return new BlockInfo(block, blockKey, blockName);
    }
    
    @Override
    public boolean isBed(BlockInfo blockInfo) {
        if (blockInfo == null || blockInfo.getBlockObject() == null) {
            return false;
        }
        try {
            Block block = (Block) blockInfo.getBlockObject();
            return block instanceof BedBlock;
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    @Override
    public IBlockInteractor.InteractionResult interactWithBlock(int[] pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null || mc.gameMode == null) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
        
        if (!(mc.player instanceof LocalPlayer)) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
        
        LocalPlayer player = (LocalPlayer) mc.player;
        
        if (pos == null || pos.length < 3) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
        
        BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
        BlockState bedState = mc.level.getBlockState(blockPos);
        Block block = bedState.getBlock();
        
        if (!(block instanceof BedBlock)) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
        
        // 检查距离
        Vec3 playerPos = player.position();
        Vec3 bedVec = Vec3.atCenterOf(blockPos);
        double distance = playerPos.distanceTo(bedVec);
        
        if (distance > 3.0) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
        
        try {
            Vec3 hitVec = bedVec.add(0, 0.5, 0);
            BlockHitResult hitResult = new BlockHitResult(
                hitVec,
                Direction.UP,
                blockPos,
                false
            );
            
            net.minecraft.world.InteractionResult mcResult = mc.gameMode.useItemOn(
                player,
                InteractionHand.MAIN_HAND,
                hitResult
            );
            
            // 将 Minecraft 的 InteractionResult 转换为接口的枚举
            if (mcResult.consumesAction()) {
                return IBlockInteractor.InteractionResult.SUCCESS;
            } else if (mcResult == net.minecraft.world.InteractionResult.FAIL) {
                return IBlockInteractor.InteractionResult.FAILED;
            } else {
                return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
            }
        } catch (Exception e) {
            return IBlockInteractor.InteractionResult.NOT_AVAILABLE;
        }
    }
}

