package com.xiaofan.api;

/**
 * Baritone 执行器接口
 * 用于执行 Baritone 命令
 */
public interface IBaritoneExecutor {
    /**
     * 执行 Baritone 命令（非阻塞）
     * @param command 命令内容（不包含 # 前缀）
     * @return 是否成功发送命令
     */
    boolean executeCommand(String command);
    
    /**
     * 检查是否是阻塞命令（需要等待执行完成）
     * @param command 命令内容
     * @return 是否是阻塞命令
     */
    boolean isBlockingCommand(String command);
    
    /**
     * 执行阻塞的 Baritone 命令，等待执行完成
     * @param command 命令内容（不包含 # 前缀）
     * @param targetPos 目标位置 [x, y, z]（用于 goto 等命令），可为 null
     */
    void executeBlockingCommand(String command, int[] targetPos);
    
    /**
     * 检查 Baritone 是否已加载
     * @return 是否已加载
     */
    boolean isBaritoneLoaded();
}

