package com.xiaofan.macro;

import com.xiaofan.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Baritone 自动任务管理器
 * 管理宏文件的加载、解析和执行
 * 
 * 此代码已迁移到 allcommon，使用抽象接口，可在 1.20.1-1.20.6 版本间复用
 */
public class BaritoneTaskManager {
    private static final String MACRO_FOLDER_NAME = "do";
    private static BaritoneTaskManager instance;
    
    private final Map<String, Macro> macros = new ConcurrentHashMap<>();
    private final Map<String, MacroExecutor> runningExecutors = new ConcurrentHashMap<>();
    private final Map<String, String> macroCurrentCommands = new ConcurrentHashMap<>(); // 跟踪每个宏当前执行的命令类型
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private File macroFolder;
    private WatchService watchService;
    private Thread watchThread;
    private boolean isInitialized = false;
    
    // 定义冲突命令组（同一组内的命令不能同时执行）
    private static final Map<String, Set<String>> CONFLICT_GROUPS = new HashMap<>();
    static {
        // 路径查找组：这些命令会互相冲突
        Set<String> pathfindingGroup = new HashSet<>();
        pathfindingGroup.add("goto");
        pathfindingGroup.add("mine");
        pathfindingGroup.add("explore");
        pathfindingGroup.add("farm");
        pathfindingGroup.add("follow");
        CONFLICT_GROUPS.put("pathfinding", pathfindingGroup);
    }
    
    private BaritoneTaskManager() {
    }
    
    public static BaritoneTaskManager getInstance() {
        if (instance == null) {
            instance = new BaritoneTaskManager();
        }
        return instance;
    }
    
    private static IMinecraftVersion getVersion() {
        return VersionProvider.getVersion();
    }
    
    private static ILogger getLogger() {
        return getVersion().getLogger();
    }
    
    /**
     * 初始化任务管理器（仅初始化文件夹，不加载宏）
     */
    public void initialize() {
        if (isInitialized) {
            return;
        }
        
        if (!VersionProvider.isInitialized()) {
            getLogger().warn("[Baritone任务] Minecraft 版本未初始化，延迟初始化");
            return;
        }
        
        IMinecraftVersion version = getVersion();
        
        // 获取配置文件夹
        File gameDir = version.getGameDirectoryProvider().getGameDirectory();
        File configDir = new File(gameDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 创建 do 文件夹
        macroFolder = new File(configDir, MACRO_FOLDER_NAME);
        if (!macroFolder.exists()) {
            macroFolder.mkdirs();
            getLogger().info("[Baritone任务] 已创建宏文件夹: {}", macroFolder.getAbsolutePath());
        }
        
        // 启动文件监听
        startFileWatcher();
        
        // 注册客户端 tick 事件（使用抽象接口）
        version.getTickHandler().registerClientTick(this::onClientTick);
        
        isInitialized = true;
        getLogger().info("[Baritone任务] 任务管理器已初始化");
    }
    
    /**
     * 客户端 tick 事件处理
     * 由平台实现调用
     */
    private void onClientTick() {
        IMinecraftVersion version = getVersion();
        IPlayerProvider playerProvider = version.getPlayerProvider();
        IWorldTimeProvider worldTimeProvider = version.getWorldTimeProvider();
        
        if (!playerProvider.isPlayerPresent() || !worldTimeProvider.isWorldPresent()) {
            return;
        }
        
        // 更新所有运行中的执行器
        for (MacroExecutor executor : new ArrayList<>(runningExecutors.values())) {
            executor.onTick();
        }
    }
    
    /**
     * 获取宏文件夹
     */
    public File getMacroFolder() {
        if (!isInitialized) {
            initialize();
        }
        return macroFolder;
    }
    
    /**
     * 手动加载宏
     */
    public void loadMacro(String macroName, Macro macro) {
        macros.put(macroName, macro);
        getLogger().info("[Baritone任务] 已加载宏: {}", macroName);
    }
    
    /**
     * 加载所有宏文件
     */
    private void loadAllMacros() {
        if (macroFolder == null || !macroFolder.exists()) {
            return;
        }
        
        File[] files = macroFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            try {
                String macroName = file.getName().replace(".txt", "");
                Macro macro = MacroParser.parse(file);
                macros.put(macroName, macro);
                getLogger().info("[Baritone任务] 已加载宏: {}", macroName);
            } catch (Exception e) {
                getLogger().error("[Baritone任务] 加载宏文件失败: " + file.getName(), e);
            }
        }
    }
    
    /**
     * 启动文件监听器
     */
    private void startFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = macroFolder.toPath();
            path.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
            
            watchThread = new Thread(() -> {
                try {
                    while (isInitialized) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }
                            
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            
                            if (fileName.toString().endsWith(".txt")) {
                                String macroName = fileName.toString().replace(".txt", "");
                                
                                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                    macros.remove(macroName);
                                    stopMacro(macroName);
                                    getLogger().info("[Baritone任务] 已删除宏: {}", macroName);
                                } else {
                                    // 重新加载宏
                                    File file = new File(macroFolder, fileName.toString());
                                    if (file.exists()) {
                                        try {
                                            Macro macro = MacroParser.parse(file);
                                            macros.put(macroName, macro);
                                            getLogger().info("[Baritone任务] 已重新加载宏: {}", macroName);
                                        } catch (Exception e) {
                                            getLogger().error("[Baritone任务] 重新加载宏失败: " + macroName, e);
                                        }
                                    }
                                }
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    getLogger().error("[Baritone任务] 文件监听器错误", e);
                }
            }, "BaritoneMacroWatcher");
            watchThread.setDaemon(true);
            watchThread.start();
            
        } catch (IOException e) {
            getLogger().error("[Baritone任务] 启动文件监听器失败", e);
        }
    }
    
    /**
     * 启动宏
     * @param macroName 宏名称
     * @throws NotFanMacroFound 如果宏不存在
     */
    public void startMacro(String macroName) throws NotFanMacroFound {
        // 如果宏未加载，先尝试加载所有宏
        if (!macros.containsKey(macroName)) {
            loadAllMacros();
        }
        
        if (!macros.containsKey(macroName)) {
            getLogger().warn("[Baritone任务] 宏不存在: {}", macroName);
            throw new NotFanMacroFound(macroName);
        }
        
        Macro macro = macros.get(macroName);
        MacroExecutor executor = new MacroExecutor(macroName, macro);
        runningExecutors.put(macroName, executor);
        
        executorService.submit(executor);
        getLogger().info("[Baritone任务] 已启动宏: {}", macroName);
    }
    
    /**
     * 停止宏
     */
    public void stopMacro(String macroName) {
        MacroExecutor executor = runningExecutors.remove(macroName);
        if (executor != null) {
            executor.stop();
            getLogger().info("[Baritone任务] 已停止宏: {}", macroName);
        }
    }
    
    /**
     * 从运行列表中移除宏（由 MacroExecutor 在完成时调用）
     */
    public void removeRunningMacro(String macroName) {
        MacroExecutor executor = runningExecutors.remove(macroName);
        macroCurrentCommands.remove(macroName);
        if (executor != null) {
            getLogger().info("[Baritone任务] 宏执行完成，已从运行列表移除: {}", macroName);
        }
    }
    
    /**
     * 检查命令冲突并处理
     */
    public boolean checkAndHandleConflict(String macroName, String command) {
        try {
            String commandType = extractCommandType(command);
            if (commandType == null) {
                return true;
            }
            
            Set<String> conflictGroup = null;
            for (Set<String> group : CONFLICT_GROUPS.values()) {
                if (group.contains(commandType)) {
                    conflictGroup = group;
                    break;
                }
            }
            
            if (conflictGroup == null) {
                macroCurrentCommands.put(macroName, commandType);
                return true;
            }
            
            for (Map.Entry<String, String> entry : macroCurrentCommands.entrySet()) {
                String otherMacro = entry.getKey();
                String otherCommand = entry.getValue();
                
                if (otherMacro.equals(macroName)) {
                    continue;
                }
                
                if (conflictGroup.contains(otherCommand)) {
                    getLogger().warn("[Baritone任务] 检测到命令冲突: 宏 {} 正在执行 {}，与宏 {} 的 {} 冲突", 
                        otherMacro, otherCommand, macroName, commandType);
                    
                    String message = String.format("[宏冲突] 宏 %s 正在执行 %s，与宏 %s 的 %s 冲突，停止宏 %s", 
                        otherMacro, otherCommand, macroName, commandType, macroName);
                    logToChatAndLogger(message);
                    
                    stopMacro(macroName);
                    return false;
                }
            }
            
            macroCurrentCommands.put(macroName, commandType);
            return true;
            
        } catch (Exception e) {
            getLogger().error("[Baritone任务] 检查命令冲突时出错", e);
            return true;
        }
    }
    
    /**
     * 清除宏的命令记录
     */
    public void clearMacroCommand(String macroName) {
        macroCurrentCommands.remove(macroName);
    }
    
    /**
     * 提取命令类型
     */
    private String extractCommandType(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }
        
        String trimmed = command.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex > 0) {
            return trimmed.substring(0, spaceIndex).toLowerCase();
        }
        return trimmed.toLowerCase();
    }
    
    /**
     * 记录到日志和聊天框
     */
    private void logToChatAndLogger(String message) {
        getLogger().info(message);
        IMinecraftVersion version = getVersion();
        version.getPlayerProvider().sendSystemMessage(message);
    }
    
    /**
     * 停止所有宏
     */
    public void stopAllMacros() {
        for (String macroName : new ArrayList<>(runningExecutors.keySet())) {
            stopMacro(macroName);
        }
    }
    
    /**
     * 获取所有宏名称
     */
    public Set<String> getMacroNames() {
        return new HashSet<>(macros.keySet());
    }
    
    /**
     * 检查宏是否在运行
     */
    public boolean isMacroRunning(String macroName) {
        return runningExecutors.containsKey(macroName);
    }
    
    /**
     * 关闭任务管理器
     */
    public void shutdown() {
        isInitialized = false;
        stopAllMacros();
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                getLogger().error("[Baritone任务] 关闭文件监听器失败", e);
            }
        }
        
        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }
        
        executorService.shutdown();
        getLogger().info("[Baritone任务] 任务管理器已关闭");
    }
    
    /**
     * 获取当前游戏时间（刻）
     * 返回服务器世界时间，不是客户端本地时间
     */
    public static long getCurrentTime() {
        if (!VersionProvider.isInitialized()) {
            return 0;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        IWorldTimeProvider worldTimeProvider = version.getWorldTimeProvider();
        
        if (!worldTimeProvider.isWorldPresent()) {
            return 0;
        }
        
        long dayTime = worldTimeProvider.getDayTime();
        version.getLogger().debug("[时间检查] 一天内时间={}", dayTime);
        
        return dayTime;
    }
    
    /**
     * 获取玩家当前位置
     * @return 玩家位置 [x, y, z]，如果玩家不存在返回 null
     */
    public static int[] getPlayerPosition() {
        if (!VersionProvider.isInitialized()) {
            return null;
        }
        
        IMinecraftVersion version = VersionProvider.getVersion();
        IPlayerProvider playerProvider = version.getPlayerProvider();
        
        if (!playerProvider.isPlayerPresent()) {
            return null;
        }
        
        return playerProvider.getPlayerPosition();
    }
}

