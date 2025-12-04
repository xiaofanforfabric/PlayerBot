package com.xiaofan.api;

/**
 * 命令执行器接口
 * 抽象命令执行相关的操作
 */
public interface ICommandExecutor {
    /**
     * 执行 Minecraft 命令
     * @param command 命令内容（不包含 / 前缀）
     * @return 是否成功发送命令
     */
    boolean executeCommand(String command);
    
    /**
     * 发送聊天消息
     * @param message 聊天消息内容
     * @return 是否成功发送
     */
    boolean sendChat(String message);
    
    /**
     * 检查是否已连接到服务器
     */
    boolean isConnected();
}

