package com.xiaofan.version;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import com.xiaofan.api.IBaritoneExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * 1.20.1 版本的 Baritone 执行器实现
 */
public class BaritoneExecutorImpl implements IBaritoneExecutor {
    @Override
    public boolean executeCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.getConnection() == null) {
            return false;
        }
        
        try {
            IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            if (baritone == null) {
                return false;
            }
            
            // 通过聊天消息发送 Baritone 命令
            String chatCommand = "#" + command;
            mc.getConnection().sendChat(chatCommand);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isBlockingCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }
        String cmd = command.trim().toLowerCase();
        return cmd.startsWith("goto ") || 
               cmd.startsWith("mine ") || 
               cmd.startsWith("explore") ||
               cmd.startsWith("follow ") ||
               cmd.startsWith("farm");
    }
    
    @Override
    public void executeBlockingCommand(String command, int[] targetPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        
        try {
            IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            if (baritone == null) {
                return;
            }
            
            // 在主游戏线程中执行命令
            if (mc.isSameThread()) {
                executeCommandInternal(command);
            } else {
                final String cmdFinal = command;
                mc.execute(() -> executeCommandInternal(cmdFinal));
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            // 等待命令执行完成
            if (targetPos != null && targetPos.length >= 3) {
                BlockPos target = new BlockPos(targetPos[0], targetPos[1], targetPos[2]);
                waitForPathfinding(baritone, target);
            } else {
                // 非 goto 命令，等待一小段时间
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void executeCommandInternal(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.getConnection() != null) {
            String chatCommand = "#" + command;
            mc.getConnection().sendChat(chatCommand);
        }
    }
    
    private void waitForPathfinding(IBaritone baritone, BlockPos targetPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }
        
        int maxWaitTime = 300000; // 最大等待5分钟
        int checkInterval = 500; // 每500ms检查一次
        int waited = 0;
        BlockPos lastPos = null;
        int stableCount = 0;
        final int TOLERANCE = 3;
        
        while (waited < maxWaitTime) {
            try {
                BlockPos currentPos = mc.player.blockPosition();
                
                if (targetPos != null) {
                    double distance = Math.sqrt(
                        Math.pow(currentPos.getX() - targetPos.getX(), 2) +
                        Math.pow(currentPos.getY() - targetPos.getY(), 2) +
                        Math.pow(currentPos.getZ() - targetPos.getZ(), 2)
                    );
                    
                    if (distance <= TOLERANCE) {
                        Thread.sleep(1000);
                        if (mc.player.blockPosition().distSqr(targetPos) <= TOLERANCE * TOLERANCE) {
                            break;
                        }
                    }
                }
                
                if (lastPos != null && currentPos.equals(lastPos)) {
                    stableCount++;
                    if (stableCount >= 4) {
                        break;
                    }
                } else {
                    stableCount = 0;
                }
                
                lastPos = currentPos;
                Thread.sleep(checkInterval);
                waited += checkInterval;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                break;
            }
        }
    }
    
    @Override
    public boolean isBaritoneLoaded() {
        try {
            IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            return baritone != null;
        } catch (Exception e) {
            return false;
        }
    }
}

