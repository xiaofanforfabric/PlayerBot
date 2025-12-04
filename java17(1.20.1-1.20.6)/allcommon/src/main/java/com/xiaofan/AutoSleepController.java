package com.xiaofan;

import com.xiaofan.api.*;

/**
 * GLFW 按键常量（避免依赖 LWJGL）
 */
class GLFWKeys {
    static final int GLFW_KEY_HOME = 268; // GLFW.GLFW_KEY_HOME
}

/**
 * 自动睡觉控制器（开关模式）
 * 按 HOME 键切换开关，开启后当游戏时间到了可以睡觉的时间段时，自动尝试在附近的床上睡觉
 */
public class AutoSleepController {
    // 可以睡觉的时间范围（游戏时间，0-24000为一个完整的一天）
    // 12500 = 晚上7点，23450 = 早上6点
    private static final long SLEEP_START_TIME = 12500L;  // 晚上7点
    private static final long SLEEP_END_TIME = 23450L;    // 早上6点
    
    // 搜索床的范围（以玩家为中心）
    private static final int SEARCH_RANGE = 8;
    
    // 防止频繁尝试睡觉的冷却时间（tick数，20 tick = 1秒）
    private static final int COOLDOWN_TICKS = 40; // 2秒
    
    private static boolean enabled = false; // 开关状态
    private static boolean initialized = false;
    private static int cooldownCounter = 0;
    private static boolean lastSleepAttemptFailed = false;
    
    /**
     * 初始化自动睡觉控制器
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        // 注册 HOME 键监听（GLFW_KEY_HOME = 268）
        version.getKeyInputHandler().registerKeyPress(GLFWKeys.GLFW_KEY_HOME, AutoSleepController::toggle);
        
        // 使用抽象接口注册客户端 Tick 事件
        version.getTickHandler().registerClientTick(AutoSleepController::onClientTick);
        
        initialized = true;
        version.getLogger().info("[自动睡觉] 自动睡觉控制器已初始化，按 HOME 键切换开关");
    }
    
    /**
     * 切换自动睡觉开关
     */
    private static void toggle() {
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        enabled = !enabled;
        IPlayerProvider playerProvider = version.getPlayerProvider();
        ILogger logger = version.getLogger();
        
        if (playerProvider.isPlayerPresent()) {
            String status = enabled ? "§a开启" : "§c关闭";
            playerProvider.sendSystemMessage("§7[自动睡觉] " + status);
        }
        logger.info("[自动睡觉] 自动睡觉已{}", enabled ? "开启" : "关闭");
    }
    
    /**
     * 获取当前开关状态
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 客户端 Tick 事件处理
     */
    private static void onClientTick() {
        // 如果未启用，直接返回
        if (!enabled) {
            cooldownCounter = 0;
            lastSleepAttemptFailed = false;
            return;
        }
        
        // 冷却时间计数
        if (cooldownCounter > 0) {
            cooldownCounter--;
            return;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        if (version == null) {
            return;
        }
        
        IPlayerProvider playerProvider = version.getPlayerProvider();
        IPlayerStatusChecker statusChecker = version.getPlayerStatusChecker();
        IWorldTimeProvider worldTimeProvider = version.getWorldTimeProvider();
        IBlockInteractor blockInteractor = version.getBlockInteractor();
        ILogger logger = version.getLogger();
        
        if (!playerProvider.isPlayerPresent() || !worldTimeProvider.isWorldPresent()) {
            return;
        }
        
        // 检查是否已经在睡觉
        if (statusChecker.isSleeping()) {
            return;
        }
        
        // 获取游戏时间（dayTime：0-24000，0是早上6点）
        long dayTime = worldTimeProvider.getDayTime();
        
        // 检查是否在可以睡觉的时间段
        boolean canSleep = false;
        if (dayTime >= SLEEP_START_TIME || dayTime < SLEEP_END_TIME) {
            canSleep = true;
        }
        
        if (!canSleep) {
            // 不在睡觉时间段，重置失败标志
            lastSleepAttemptFailed = false;
            return;
        }
        
        // 如果上次尝试失败，增加冷却时间
        if (lastSleepAttemptFailed) {
            cooldownCounter = COOLDOWN_TICKS * 2; // 失败后等待更长时间
            lastSleepAttemptFailed = false;
            return;
        }
        
        // 尝试找床并睡觉
        int[] playerPos = playerProvider.getPlayerPosition();
        if (playerPos == null || playerPos.length < 3) {
            return;
        }
        
        int[] bedPos = findNearbyBed(playerPos, blockInteractor, SEARCH_RANGE);
        if (bedPos != null) {
            try {
                // 计算距离
                double distance = calculateDistance(playerPos, bedPos);
                
                if (distance > 3.0) {
                    // 床太远，尝试靠近（简单的移动逻辑）
                    logger.debug("[自动睡觉] 床距离较远 ({} 格)，尝试靠近", String.format("%.1f", distance));
                }
                
                // 尝试在床上睡觉
                if (distance <= 3.0) {
                    logger.info("[自动睡觉] 尝试在床 ({}, {}, {}) 上睡觉，距离: {} 格", 
                        bedPos[0], bedPos[1], bedPos[2], String.format("%.1f", distance));
                    
                    // 使用抽象接口与床交互
                    IBlockInteractor.InteractionResult result = blockInteractor.interactWithBlock(bedPos);
                    
                    if (result == IBlockInteractor.InteractionResult.SUCCESS) {
                        logger.info("[自动睡觉] 成功尝试在床上睡觉");
                        lastSleepAttemptFailed = false;
                    } else {
                        logger.debug("[自动睡觉] 无法在床上睡觉（可能床被占用或距离太远）");
                        lastSleepAttemptFailed = true;
                    }
                } else {
                    logger.debug("[自动睡觉] 床距离太远 ({} 格)，需要靠近", String.format("%.1f", distance));
                    lastSleepAttemptFailed = true;
                }
                
                // 设置冷却时间
                cooldownCounter = COOLDOWN_TICKS;
                
            } catch (Exception e) {
                logger.error("[自动睡觉] 处理睡觉逻辑时发生错误", e);
                lastSleepAttemptFailed = true;
                cooldownCounter = COOLDOWN_TICKS;
            }
        } else {
            // 没有找到床
            if (cooldownCounter == 0) {
                logger.debug("[自动睡觉] 附近没有找到床（搜索范围：{}格）", SEARCH_RANGE);
                cooldownCounter = COOLDOWN_TICKS * 3; // 没有床时等待更长时间
            }
        }
    }
    
    /**
     * 在玩家附近查找床
     * @param playerPos 玩家位置 [x, y, z]
     * @param blockInteractor 方块交互器
     * @param searchRange 搜索范围
     * @return 床的位置 [x, y, z]，如果没找到返回 null
     */
    private static int[] findNearbyBed(int[] playerPos, IBlockInteractor blockInteractor, int searchRange) {
        if (playerPos == null || playerPos.length < 3 || blockInteractor == null) {
            return null;
        }
        
        int[] closestBed = null;
        double closestDistance = Double.MAX_VALUE;
        
        // 在搜索范围内查找床
        for (int x = -searchRange; x <= searchRange; x++) {
            for (int y = -searchRange; y <= searchRange; y++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    int[] checkPos = new int[]{
                        playerPos[0] + x,
                        playerPos[1] + y,
                        playerPos[2] + z
                    };
                    
                    BlockInfo blockInfo = blockInteractor.getBlockState(checkPos);
                    if (blockInfo != null && blockInteractor.isBed(blockInfo)) {
                        // 检查床是否可用（没有被占用等）
                        // 注意：在客户端可能无法完全检查占用状态
                        
                        double distance = calculateDistance(playerPos, checkPos);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestBed = checkPos;
                        }
                    }
                }
            }
        }
        
        return closestBed;
    }
    
    /**
     * 计算两点之间的距离
     */
    private static double calculateDistance(int[] pos1, int[] pos2) {
        if (pos1 == null || pos2 == null || pos1.length < 3 || pos2.length < 3) {
            return Double.MAX_VALUE;
        }
        
        double dx = pos1[0] - pos2[0];
        double dy = pos1[1] - pos2[1];
        double dz = pos1[2] - pos2[2];
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

