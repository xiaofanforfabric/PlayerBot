package com.xiaofan.macro;

import com.xiaofan.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 宏执行器
 * 执行宏命令
 * 
 * 此代码已迁移到 allcommon，使用抽象接口，可在 1.20.1-1.20.6 版本间复用
 */
public class MacroExecutor implements Runnable {
    private final String macroName;
    private final Macro macro;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    
    public MacroExecutor(String macroName, Macro macro) {
        this.macroName = macroName;
        this.macro = macro;
    }
    
    private static IMinecraftVersion getVersion() {
        return VersionProvider.getVersion();
    }
    
    private static ILogger getLogger() {
        return getVersion().getLogger();
    }
    
    @Override
    public void run() {
        logToChatAndLogger("[宏执行] 开始执行宏: " + macroName);
        getLogger().info("[宏执行] 开始执行宏: {}", macroName);
        
        try {
            executeMacro(macro);
        } catch (Exception e) {
            logToChatAndLogger("[宏执行] 执行宏时出错: " + macroName + " - " + e.getMessage());
            getLogger().error("[宏执行] 执行宏时出错: " + macroName, e);
        } finally {
            logToChatAndLogger("[宏执行] 宏执行结束: " + macroName);
            getLogger().info("[宏执行] 宏执行结束: {}", macroName);
            
            BaritoneTaskManager.getInstance().removeRunningMacro(macroName);
        }
    }
    
    /**
     * 同时记录到日志和聊天框
     */
    private void logToChatAndLogger(String message) {
        getLogger().info(message);
        IMinecraftVersion version = getVersion();
        version.getPlayerProvider().sendSystemMessage("§7[宏] §r" + message);
    }
    
    /**
     * 执行宏
     */
    private void executeMacro(Macro macro) {
        String macroInfo = String.format("[宏执行] 宏包含 %d 个命令", macro.commands.size());
        logToChatAndLogger(macroInfo);
        getLogger().info("[宏执行] 宏包含 {} 个命令", macro.commands.size());
        
        if (macro.commands.isEmpty()) {
            logToChatAndLogger("[宏执行] 宏文件没有解析出任何命令，请检查宏文件格式");
            getLogger().warn("[宏执行] 宏文件没有解析出任何命令，请检查宏文件格式");
            return;
        }
        
        boolean hasConditional = macro.commands.stream()
            .anyMatch(cmd -> cmd instanceof IfStatement);
        
        if (hasConditional) {
            logToChatAndLogger("[宏执行] 宏包含条件语句，将循环执行");
            getLogger().info("[宏执行] 宏包含条件语句，将循环执行");
            while (!stopped.get()) {
                for (int i = 0; i < macro.commands.size(); i++) {
                    if (stopped.get()) {
                        getLogger().info("[宏执行] 宏已被停止，停止执行剩余命令");
                        return;
                    }
                    MacroCommand cmd = macro.commands.get(i);
                    String cmdInfo = String.format("[宏执行] 执行第 %d 个命令: %s", i + 1, cmd.getClass().getSimpleName());
                    logToChatAndLogger(cmdInfo);
                    getLogger().info("[宏执行] 执行第 {} 个命令: {}", i + 1, cmd.getClass().getSimpleName());
                    try {
                        cmd.execute(this);
                        if (cmd instanceof IfStatement) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        getLogger().error("[宏执行] 执行命令时出错", e);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } else {
            for (int i = 0; i < macro.commands.size(); i++) {
                if (stopped.get()) {
                    getLogger().info("[宏执行] 宏已被停止，停止执行剩余命令");
                    break;
                }
                MacroCommand cmd = macro.commands.get(i);
                getLogger().info("[宏执行] 执行第 {} 个命令: {}", i + 1, cmd.getClass().getSimpleName());
                try {
                    cmd.execute(this);
                } catch (Exception e) {
                    getLogger().error("[宏执行] 执行命令时出错", e);
                }
            }
        }
    }
    
    /**
     * 执行 do 命令
     */
    public void executeDoCommand(String content) {
        if (stopped.get()) {
            return;
        }
        
        if (content.startsWith("fun ")) {
            String funcName = content.substring(4).trim();
            if (funcName.startsWith("\"") && funcName.endsWith("\"")) {
                funcName = funcName.substring(1, funcName.length() - 1);
            } else if (funcName.endsWith("\"")) {
                int quoteStart = funcName.indexOf("\"");
                if (quoteStart >= 0) {
                    funcName = funcName.substring(quoteStart + 1, funcName.length() - 1);
                }
            }
            callFunction(funcName);
            return;
        }
        
        if (content.equals("end")) {
            logToChatAndLogger("[宏执行] 执行 end 命令，停止当前执行上下文");
            getLogger().info("[宏执行] 执行 end 命令，停止当前执行上下文");
            stop();
            return;
        }
        
        if (content.startsWith("wait")) {
            String waitContent = content.substring(4).trim();
            executeWaitCommand(waitContent);
            return;
        }
        
        if (content.startsWith("/")) {
            String command = content.substring(1).trim();
            executeMinecraftCommand(command);
            return;
        }
        
        if (content.startsWith("#")) {
            String baritoneCmd = content.substring(1).trim();
            IMinecraftVersion version = getVersion();
            IBaritoneExecutor baritoneExecutor = version.getBaritoneExecutor();
            
            if (!baritoneExecutor.isBaritoneLoaded()) {
                getLogger().warn("[宏执行] Baritone 未加载，无法执行命令");
                return;
            }
            
            boolean isBlocking = baritoneExecutor.isBlockingCommand(baritoneCmd);
            if (isBlocking) {
                logToChatAndLogger("[宏执行] 检测到阻塞命令: " + baritoneCmd + "，将等待执行完成");
                getLogger().info("[宏执行] 检测到阻塞命令: {}，将等待执行完成", baritoneCmd);
                
                int[] targetPos = null;
                String cmd = baritoneCmd.trim().toLowerCase();
                if (cmd.startsWith("goto ")) {
                    String[] parts = baritoneCmd.trim().split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            int z = Integer.parseInt(parts[3]);
                            targetPos = new int[]{x, y, z};
                            getLogger().info("[宏执行] 解析到目标坐标: ({}, {}, {})", x, y, z);
                        } catch (NumberFormatException e) {
                            getLogger().warn("[宏执行] 无法解析 goto 命令的坐标: {}", baritoneCmd);
                        }
                    }
                }
                
                baritoneExecutor.executeBlockingCommand(baritoneCmd, targetPos);
                BaritoneTaskManager.getInstance().clearMacroCommand(macroName);
            } else {
                baritoneExecutor.executeCommand(baritoneCmd);
            }
        } else {
            getLogger().warn("[宏执行] 未知命令: {}", content);
        }
    }
    
    /**
     * 执行原版 Minecraft 命令
     */
    private void executeMinecraftCommand(String command) {
        if (stopped.get()) {
            return;
        }
        
        logToChatAndLogger("[宏执行] 准备执行原版命令: /" + command);
        getLogger().info("[宏执行] 准备执行原版命令: /{}", command);
        
        IMinecraftVersion version = getVersion();
        ICommandExecutor commandExecutor = version.getCommandExecutor();
        IPlayerProvider playerProvider = version.getPlayerProvider();
        
        if (!playerProvider.isPlayerPresent()) {
            getLogger().warn("[宏执行] 无法执行原版命令，玩家未初始化");
            return;
        }
        
        if (!commandExecutor.isConnected()) {
            getLogger().warn("[宏执行] 无法执行原版命令，未连接到服务器");
            return;
        }
        
        if (playerProvider.isOnMainThread()) {
            executeMinecraftCommandInternal(command);
        } else {
            final String cmd = command;
            playerProvider.executeOnMainThread(() -> executeMinecraftCommandInternal(cmd));
        }
    }
    
    /**
     * 内部方法：实际执行原版命令
     */
    private void executeMinecraftCommandInternal(String command) {
        try {
            IMinecraftVersion version = getVersion();
            ICommandExecutor commandExecutor = version.getCommandExecutor();
            IPlayerProvider playerProvider = version.getPlayerProvider();
            
            if (!playerProvider.isPlayerPresent() || !commandExecutor.isConnected()) {
                getLogger().warn("[宏执行] 无法执行原版命令，连接或玩家未初始化");
                return;
            }
            
            boolean success = commandExecutor.executeCommand(command);
            if (success) {
                logToChatAndLogger("[宏执行] ✓ 已发送原版命令: /" + command);
                getLogger().info("[宏执行] ✓ 已发送原版命令: /{}", command);
            } else {
                getLogger().warn("[宏执行] 发送原版命令失败: /{}", command);
            }
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 执行原版命令时出错: /" + command, e);
            logToChatAndLogger("[宏执行] 执行原版命令时出错: /" + command + " - " + e.getMessage());
        }
    }
    
    /**
     * 调用函数
     */
    private void callFunction(String funcName) {
        logToChatAndLogger("[宏执行] 尝试调用函数: " + funcName);
        logToChatAndLogger("[宏执行] 当前宏包含的函数: " + macro.functions.keySet());
        getLogger().info("[宏执行] 尝试调用函数: {}", funcName);
        getLogger().info("[宏执行] 当前宏包含的函数: {}", macro.functions.keySet());
        
        Function func = macro.functions.get(funcName);
        if (func == null) {
            logToChatAndLogger("[宏执行] 函数不存在: " + funcName + "，可用函数: " + macro.functions.keySet());
            getLogger().warn("[宏执行] 函数不存在: {}，可用函数: {}", funcName, macro.functions.keySet());
            return;
        }
        
        String funcInfo = String.format("[宏执行] 调用函数: %s (后台执行: %s)，函数包含 %d 个命令", 
            funcName, func.isBackground, func.commands.size());
        logToChatAndLogger(funcInfo);
        getLogger().info("[宏执行] 调用函数: {} (后台执行: {})，函数包含 {} 个命令", funcName, func.isBackground, func.commands.size());
        
        func.functions = macro.functions;
        
        if (func.isBackground) {
            new Thread(() -> {
                getLogger().info("[宏执行] 函数 {} 在后台线程开始执行", funcName);
                executeFunctionCommands(func);
                getLogger().info("[宏执行] 函数 {} 在后台线程执行完成", funcName);
            }, "MacroFunction-" + funcName).start();
        } else {
            getLogger().info("[宏执行] 函数 {} 在主线程执行", funcName);
            executeFunctionCommands(func);
        }
    }
    
    /**
     * 执行函数命令
     */
    private void executeFunctionCommands(Function func) {
        boolean hasConditional = func.commands.stream()
            .anyMatch(cmd -> cmd instanceof IfStatement);
        
        if (hasConditional) {
            getLogger().info("[宏执行] 函数 {} 包含条件语句，将循环执行", func.name);
            while (!stopped.get()) {
                for (MacroCommand cmd : func.commands) {
                    if (stopped.get()) {
                        return;
                    }
                    try {
                        cmd.execute(this);
                        if (cmd instanceof IfStatement) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        getLogger().error("[宏执行] 函数 " + func.name + " 执行命令时出错", e);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } else {
            for (MacroCommand cmd : func.commands) {
                if (stopped.get()) {
                    break;
                }
                try {
                    cmd.execute(this);
                } catch (Exception e) {
                    getLogger().error("[宏执行] 函数 " + func.name + " 执行命令时出错", e);
                }
            }
        }
    }
    
    /**
     * 执行 wait 命令
     */
    public void executeWaitCommand(String content) {
        if (stopped.get()) {
            return;
        }
        
        try {
            content = content.trim();
            
            if (content.isEmpty()) {
                logToChatAndLogger("[宏执行] 执行 wait 命令，将一直阻塞直到宏结束");
                getLogger().info("[宏执行] 执行 wait 命令，将一直阻塞直到宏结束");
                
                while (!stopped.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        getLogger().warn("[宏执行] wait 命令被中断");
                        break;
                    }
                }
                
                logToChatAndLogger("[宏执行] wait 命令结束（宏已停止）");
                getLogger().info("[宏执行] wait 命令结束（宏已停止）");
                return;
            }
            
            long waitTimeMs = 0;
            Pattern pattern = Pattern.compile("^(\\d+)([smh])$");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.matches()) {
                long value = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);
                
                switch (unit) {
                    case "s":
                        waitTimeMs = value * 1000;
                        break;
                    case "m":
                        waitTimeMs = value * 60 * 1000;
                        break;
                    case "h":
                        waitTimeMs = value * 60 * 60 * 1000;
                        break;
                    default:
                        getLogger().warn("[宏执行] wait 命令格式错误: {}", content);
                        logToChatAndLogger("[宏执行] wait 命令格式错误: " + content);
                        return;
                }
                
                logToChatAndLogger(String.format("[宏执行] 执行 wait 命令，将阻塞 %d%s", value, unit));
                getLogger().info("[宏执行] 执行 wait 命令，将阻塞 {}ms ({} {})", waitTimeMs, value, unit);
                
                long startTime = System.currentTimeMillis();
                while (!stopped.get() && (System.currentTimeMillis() - startTime) < waitTimeMs) {
                    try {
                        long remaining = waitTimeMs - (System.currentTimeMillis() - startTime);
                        Thread.sleep(Math.min(100, remaining));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        getLogger().warn("[宏执行] wait 命令被中断");
                        break;
                    }
                }
                
                if (stopped.get()) {
                    logToChatAndLogger("[宏执行] wait 命令提前结束（宏已停止）");
                    getLogger().info("[宏执行] wait 命令提前结束（宏已停止）");
                } else {
                    logToChatAndLogger("[宏执行] wait 命令完成");
                    getLogger().info("[宏执行] wait 命令完成");
                }
            } else {
                getLogger().warn("[宏执行] wait 命令格式错误: {}，正确格式: wait、wait xs、wait xm、wait xh", content);
                logToChatAndLogger("[宏执行] wait 命令格式错误: " + content + "，正确格式: wait、wait xs、wait xm、wait xh");
            }
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 执行 wait 命令时出错", e);
            logToChatAndLogger("[宏执行] 执行 wait 命令时出错: " + e.getMessage());
        }
    }
    
    /**
     * 执行 check 命令
     */
    public void executeCheckCommand(CheckCommand cmd) {
        if (stopped.get()) {
            return;
        }
        
        try {
            boolean condition = false;
            
            switch (cmd.type) {
                case ITEM:
                    condition = checkItem(cmd);
                    break;
                case NOTHAVE:
                    condition = checkNotHaveItem(cmd);
                    break;
                case POSITION:
                    condition = checkPosition(cmd);
                    break;
                case TIME:
                    condition = checkTime(cmd);
                    break;
            }
            
            if (condition) {
                logToChatAndLogger(String.format("[宏执行] check 条件满足，执行动作: %s", cmd.action));
                getLogger().info("[宏执行] check 条件满足，执行动作: {}", cmd.action);
                
                String actionToExecute = cmd.action;
                if (actionToExecute.startsWith("do ")) {
                    actionToExecute = actionToExecute.substring(3).trim();
                }
                
                if (actionToExecute.equals("end")) {
                    logToChatAndLogger("[宏执行] check 条件满足，执行 end 命令，停止整个宏");
                    getLogger().info("[宏执行] check 条件满足，执行 end 命令，停止整个宏");
                    stop();
                    return;
                }
                
                executeDoCommand(actionToExecute);
            } else {
                logToChatAndLogger("[宏执行] check 条件不满足，继续执行下一个命令");
                getLogger().info("[宏执行] check 条件不满足，继续执行下一个命令");
            }
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 执行 check 命令时出错", e);
            logToChatAndLogger("[宏执行] 执行 check 命令时出错: " + e.getMessage());
        }
    }
    
    /**
     * 执行 run 命令
     */
    public void executeRunCommand(String macroName) {
        if (stopped.get()) {
            return;
        }
        
        try {
            logToChatAndLogger(String.format("[宏执行] 执行 run 命令，启动宏: %s", macroName));
            getLogger().info("[宏执行] 执行 run 命令，启动宏: {}", macroName);
            
            BaritoneTaskManager.getInstance().startMacro(macroName);
            
            logToChatAndLogger(String.format("[宏执行] run 命令执行成功，已启动宏: %s", macroName));
            getLogger().info("[宏执行] run 命令执行成功，已启动宏: {}", macroName);
            
        } catch (NotFanMacroFound e) {
            getLogger().error("[宏执行] run 命令失败: " + e.getMessage());
            logToChatAndLogger("[宏执行] run 命令失败: " + e.getMessage());
        } catch (Exception e) {
            getLogger().error("[宏执行] 执行 run 命令时出错", e);
            logToChatAndLogger("[宏执行] 执行 run 命令时出错: " + e.getMessage());
        }
    }
    
    /**
     * 执行 IfStatement
     */
    public void executeIfStatement(IfStatement ifStmt) {
        boolean condition = false;
        IMinecraftVersion version = getVersion();
        
        if (ifStmt.type == IfStatement.Type.POSITION) {
            int[] pos = BaritoneTaskManager.getPlayerPosition();
            if (pos != null) {
                int tolerance = 2;
                condition = Math.abs(pos[0] - ifStmt.x) <= tolerance 
                        && Math.abs(pos[1] - ifStmt.y) <= tolerance 
                        && Math.abs(pos[2] - ifStmt.z) <= tolerance;
                String coordInfo = String.format("[宏执行] 坐标检查: 目标=(%d,%d,%d), 当前位置=(%d,%d,%d), 容差=%d, 结果=%s", 
                    ifStmt.x, ifStmt.y, ifStmt.z, pos[0], pos[1], pos[2], tolerance, condition);
                logToChatAndLogger(coordInfo);
                getLogger().info("[宏执行] 坐标检查: 目标=({},{},{}), 当前位置=({},{},{}), 容差={}, 结果={}", 
                    ifStmt.x, ifStmt.y, ifStmt.z, pos[0], pos[1], pos[2], tolerance, condition);
            } else {
                getLogger().warn("[宏执行] 无法获取玩家位置");
            }
        } else if (ifStmt.type == IfStatement.Type.TIME) {
            long currentTime = BaritoneTaskManager.getCurrentTime();
            long tolerance = 50;
            
            switch (ifStmt.timeComparison) {
                case EQUAL:
                    condition = Math.abs(currentTime - ifStmt.time) <= tolerance;
                    break;
                case GREATER_EQUAL:
                    condition = currentTime >= (ifStmt.time - tolerance);
                    break;
                case LESS_EQUAL:
                    condition = currentTime <= (ifStmt.time + tolerance);
                    break;
            }
            String timeInfo = String.format("[宏执行] 时间检查: 目标=%d, 当前时间=%d, 容差=%d, 结果=%s", 
                ifStmt.time, currentTime, tolerance, condition);
            logToChatAndLogger(timeInfo);
            getLogger().info("[宏执行] 时间检查: 目标={}, 当前时间={}, 容差={}, 结果={}", 
                ifStmt.time, currentTime, tolerance, condition);
        }
        
        List<MacroCommand> commandsToExecute = condition ? ifStmt.ifCommands : ifStmt.elseCommands;
        String ifInfo = String.format("[宏执行] IfStatement 条件=%s, 将执行 %d 个命令 (if分支: %d, else分支: %d)", 
            condition, commandsToExecute.size(), ifStmt.ifCommands.size(), ifStmt.elseCommands.size());
        logToChatAndLogger(ifInfo);
        getLogger().info("[宏执行] IfStatement 条件={}, 将执行 {} 个命令 (if分支: {}, else分支: {})", 
            condition, commandsToExecute.size(), ifStmt.ifCommands.size(), ifStmt.elseCommands.size());
        
        for (MacroCommand cmd : commandsToExecute) {
            if (stopped.get()) {
                getLogger().info("[宏执行] IfStatement 分支执行被停止（遇到 end 命令）");
                break;
            }
            String branchCmdInfo = "[宏执行] 执行 IfStatement 分支中的命令: " + cmd.getClass().getSimpleName();
            logToChatAndLogger(branchCmdInfo);
            getLogger().info("[宏执行] 执行 IfStatement 分支中的命令: {}", cmd.getClass().getSimpleName());
            cmd.execute(this);
        }
        
        logToChatAndLogger("[宏执行] IfStatement 分支执行完成，继续执行后续命令");
        getLogger().info("[宏执行] IfStatement 分支执行完成，继续执行后续命令");
    }
    
    /**
     * 检查物品
     */
    private boolean checkItem(CheckCommand cmd) {
        try {
            IMinecraftVersion version = getVersion();
            IPlayerProvider playerProvider = version.getPlayerProvider();
            IItemRegistry itemRegistry = version.getItemRegistry();
            
            if (!playerProvider.isPlayerPresent()) {
                return false;
            }
            
            int totalCount = 0;
            String itemNameLower = cmd.itemName.toLowerCase();
            boolean isToolCheck = cmd.itemType != null && !cmd.itemType.isEmpty();
            
            int inventorySize = playerProvider.getInventorySize();
            for (int i = 0; i < inventorySize; i++) {
                ItemInfo itemInfo = playerProvider.getItemInSlot(i);
                if (itemInfo == null || itemInfo.isEmpty()) {
                    continue;
                }
                
                String itemName = itemInfo.getItemName();
                String itemNameLowerActual = itemName.toLowerCase();
                
                boolean nameMatches = false;
                
                if (isToolCheck) {
                    nameMatches = itemNameLowerActual.equals(itemNameLower) || 
                                 itemNameLowerActual.contains(itemNameLower) ||
                                 itemNameLower.contains(itemNameLowerActual);
                } else {
                    nameMatches = itemNameLowerActual.equals(itemNameLower);
                }
                
                if (nameMatches) {
                    if (isToolCheck) {
                        boolean isTool = itemRegistry.isToolItem(itemInfo);
                        if (!isTool) {
                            throw new IllegalArgumentException("物品类型错误: " + itemName + " 不是工具类物品，不能指定 type");
                        }
                        
                        String toolMaterial = itemRegistry.getToolMaterial(itemInfo);
                        if (toolMaterial == null || !toolMaterial.equalsIgnoreCase(cmd.itemType)) {
                            continue;
                        }
                    } else {
                        boolean isTool = itemRegistry.isToolItem(itemInfo);
                        if (isTool) {
                            continue;
                        }
                    }
                    
                    totalCount += itemInfo.getCount();
                }
            }
            
            boolean result = totalCount >= cmd.quantity;
            logToChatAndLogger(String.format("[宏执行] 物品检查: 需要 %s (type=%s, quantity=%d), 找到数量=%d, 结果=%s", 
                cmd.itemName, cmd.itemType, cmd.quantity, totalCount, result));
            getLogger().info("[宏执行] 物品检查: 需要 {} (type={}, quantity={}), 找到数量={}, 结果={}", 
                cmd.itemName, cmd.itemType, cmd.quantity, totalCount, result);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            getLogger().error("[宏执行] 物品检查错误: " + e.getMessage());
            logToChatAndLogger("[宏执行] 物品检查错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            getLogger().error("[宏执行] 检查物品时出错", e);
            return false;
        }
    }
    
    /**
     * 检查没有物品
     */
    private boolean checkNotHaveItem(CheckCommand cmd) {
        try {
            IMinecraftVersion version = getVersion();
            IPlayerProvider playerProvider = version.getPlayerProvider();
            
            if (!playerProvider.isPlayerPresent()) {
                return true;
            }
            
            String itemNameLower = cmd.itemName.toLowerCase();
            
            int inventorySize = playerProvider.getInventorySize();
            for (int i = 0; i < inventorySize; i++) {
                ItemInfo itemInfo = playerProvider.getItemInSlot(i);
                if (itemInfo == null || itemInfo.isEmpty()) {
                    continue;
                }
                
                String itemName = itemInfo.getItemName();
                String itemNameLowerActual = itemName.toLowerCase();
                
                boolean nameMatches = itemNameLowerActual.equals(itemNameLower);
                
                if (nameMatches) {
                    logToChatAndLogger(String.format("[宏执行] nothave 检查: 需要没有 %s, 但找到了 %s (数量=%d), 结果=false", 
                        cmd.itemName, itemName, itemInfo.getCount()));
                    getLogger().info("[宏执行] nothave 检查: 需要没有 {}, 但找到了 {} (数量={}), 结果=false", 
                        cmd.itemName, itemName, itemInfo.getCount());
                    return false;
                }
            }
            
            logToChatAndLogger(String.format("[宏执行] nothave 检查: 需要没有 %s, 背包中没有, 结果=true", cmd.itemName));
            getLogger().info("[宏执行] nothave 检查: 需要没有 {}, 背包中没有, 结果=true", cmd.itemName);
            return true;
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 检查 nothave 时出错", e);
            return true;
        }
    }
    
    /**
     * 检查位置
     */
    private boolean checkPosition(CheckCommand cmd) {
        try {
            IMinecraftVersion version = getVersion();
            IPlayerProvider playerProvider = version.getPlayerProvider();
            
            if (!playerProvider.isPlayerPresent()) {
                return false;
            }
            
            int[] pos = playerProvider.getPlayerPosition();
            if (pos == null) {
                return false;
            }
            
            int tolerance = 2;
            boolean result = Math.abs(pos[0] - cmd.x) <= tolerance &&
                           Math.abs(pos[1] - cmd.y) <= tolerance &&
                           Math.abs(pos[2] - cmd.z) <= tolerance;
            
            logToChatAndLogger(String.format("[宏执行] 位置检查: 目标=(%d,%d,%d), 当前位置=(%d,%d,%d), 容差=%d, 结果=%s", 
                cmd.x, cmd.y, cmd.z, pos[0], pos[1], pos[2], tolerance, result));
            getLogger().info("[宏执行] 位置检查: 目标=({},{},{}), 当前位置=({},{},{}), 容差={}, 结果={}", 
                cmd.x, cmd.y, cmd.z, pos[0], pos[1], pos[2], tolerance, result);
            
            return result;
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 检查位置时出错", e);
            return false;
        }
    }
    
    /**
     * 检查时间
     */
    private boolean checkTime(CheckCommand cmd) {
        try {
            IMinecraftVersion version = getVersion();
            IWorldTimeProvider worldTimeProvider = version.getWorldTimeProvider();
            
            if (!worldTimeProvider.isWorldPresent()) {
                return false;
            }
            
            long currentTime = worldTimeProvider.getDayTime();
            int timeTolerance = 50;
            
            boolean result = Math.abs(currentTime - cmd.time) <= timeTolerance;
            
            logToChatAndLogger(String.format("[宏执行] 时间检查: 目标=%d, 当前时间=%d, 容差=%d, 结果=%s", 
                cmd.time, currentTime, timeTolerance, result));
            getLogger().info("[宏执行] 时间检查: 目标={}, 当前时间={}, 容差={}, 结果={}", 
                cmd.time, currentTime, timeTolerance, result);
            
            return result;
            
        } catch (Exception e) {
            getLogger().error("[宏执行] 检查时间时出错", e);
            return false;
        }
    }
    
    /**
     * 游戏刻更新
     */
    public void onTick() {
        // 可以在这里检查时间条件等
    }
    
    /**
     * 停止执行
     */
    public void stop() {
        stopped.set(true);
        BaritoneTaskManager.getInstance().clearMacroCommand(macroName);
    }
    
    /**
     * 检查是否已停止
     */
    public boolean isStopped() {
        return stopped.get();
    }
}

