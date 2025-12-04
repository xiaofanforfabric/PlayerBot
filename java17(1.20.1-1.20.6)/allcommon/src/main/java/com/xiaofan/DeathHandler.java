package com.xiaofan;

import com.xiaofan.api.*;
import com.xiaofan.macro.BaritoneTaskManager;

/**
 * 死亡处理
 * 通过 Tick 事件检测玩家死亡，死亡时立即停止所有 Baritone 任务和宏
 * 适用于服务器设置了立即重生的场景（没有死亡菜单）
 */
public class DeathHandler {
    private static boolean wasDead = false; // 上次检查时的死亡状态
    private static boolean initialized = false;
    
    /**
     * 初始化死亡处理器
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            version.getLogger().error("[死亡处理] 版本提供者未初始化，无法初始化死亡处理器");
            return;
        }
        
        // 使用抽象接口注册客户端 Tick 事件
        version.getTickHandler().registerClientTick(DeathHandler::onClientTick);
        initialized = true;
        version.getLogger().info("[死亡处理] 死亡处理器已初始化");
    }
    
    /**
     * 客户端 Tick 事件处理
     */
    private static void onClientTick() {
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        try {
            IPlayerProvider playerProvider = version.getPlayerProvider();
            IPlayerStatusChecker statusChecker = version.getPlayerStatusChecker();
            ILogger logger = version.getLogger();
            IBaritoneExecutor baritoneExecutor = version.getBaritoneExecutor();
            
            if (!playerProvider.isPlayerPresent()) {
                wasDead = false;
                return;
            }
            
            // 确保在服务器模式下（不是单人存档）
            if (statusChecker.isSingleplayer()) {
                // 单人存档，跳过检测
                wasDead = false;
                return;
            }
            
            // 检查玩家是否死亡（服务器模式下的死亡状态）
            boolean isDead = statusChecker.isDeadOrDying();
            
            // 检测从存活到死亡的状态变化
            if (!wasDead && isDead) {
                logger.info("[死亡处理] 检测到玩家死亡（服务器模式），正在停止所有 Baritone 任务和宏");
                
                // 在主游戏线程中执行
                playerProvider.executeOnMainThread(() -> {
                    try {
                        stopAllBaritoneTasks(version);
                    } catch (Exception e) {
                        logger.error("[死亡处理] 停止 Baritone 任务时出错", e);
                    }
                });
            }
            
            // 更新状态
            wasDead = isDead;
            
        } catch (Exception e) {
            // version 已在方法开始处获取
            if (version != null) {
                version.getLogger().error("[死亡处理] 检测死亡状态时出错", e);
            }
        }
    }
    
    /**
     * 停止所有 Baritone 任务和宏
     */
    private static void stopAllBaritoneTasks(IMinecraftVersion version) {
        ILogger logger = version.getLogger();
        IBaritoneExecutor baritoneExecutor = version.getBaritoneExecutor();
        IPlayerProvider playerProvider = version.getPlayerProvider();
        
        try {
            logger.info("[死亡处理] 正在停止所有 Baritone 任务和宏...");
            
            // 1. 停止所有正在运行的宏
            BaritoneTaskManager.getInstance().stopAllMacros();
            logger.info("[死亡处理] 已停止所有宏");
            
            // 2. 执行 Baritone 的 stop 命令，停止所有 Baritone 任务
            try {
                if (baritoneExecutor.isBaritoneLoaded()) {
                    // 通过抽象接口执行 stop 命令
                    baritoneExecutor.executeCommand("stop");
                    logger.info("[死亡处理] 已执行 Baritone stop 命令");
                    playerProvider.sendSystemMessage("§7[死亡处理] 已停止所有 Baritone 任务和宏");
                } else {
                    logger.debug("[死亡处理] Baritone 未加载，跳过 stop 命令");
                }
            } catch (Exception e) {
                logger.warn("[死亡处理] 执行 Baritone stop 命令失败: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("[死亡处理] 停止 Baritone 任务时出错", e);
        }
    }
}

