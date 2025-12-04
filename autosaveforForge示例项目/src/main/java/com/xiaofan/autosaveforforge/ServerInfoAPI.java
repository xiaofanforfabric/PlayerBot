package com.xiaofan.autosaveforforge;

import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
// import org.json.JSONObject; // 改为手动解析 JSON，避免运行时依赖问题
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Forge 版本的服务器信息 API
 * 端口: 2000
 * 端点:
 * - GET /need_server_info - 获取在线玩家信息
 * - GET /get_server_last_message - 获取最后一条聊天消息
 * - POST /send_message_to_server - 发送消息到服务器
 */
public class ServerInfoAPI {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int HTTP_PORT = 2000;
    private static HttpServer httpServer;
    private static boolean isRunning = false;
    
    // 聊天消息队列（线程安全）
    private static final ConcurrentLinkedQueue<String> chatMessageQueue = new ConcurrentLinkedQueue<>();

    /**
     * 初始化HTTP服务器和消息监听
     */
    public static void initialize() {
        if (isRunning) {
            return;
        }

        // 注册消息监听器
        MinecraftForge.EVENT_BUS.register(new ChatMessageListener());

        try {
            // 先尝试停止可能存在的旧服务器
            stop();

            httpServer = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
            httpServer.createContext("/need_server_info", new ServerInfoHandler());
            httpServer.createContext("/get_server_last_message", new LastMessageHandler());
            httpServer.createContext("/send_message_to_server", new SendMessageHandler());
            httpServer.createContext("/debug", new DebugHandler()); // 添加调试界面
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.start();
            isRunning = true;
            
            LOGGER.info("[服务器信息API] HTTP服务器已启动 (端口: {})", HTTP_PORT);
            
        } catch (java.net.BindException e) {
            LOGGER.error("[服务器信息API] 端口 {} 已被占用", HTTP_PORT);
            isRunning = false;
        } catch (IOException e) {
            LOGGER.error("[服务器信息API] 启动失败: {}", e.getMessage(), e);
            isRunning = false;
        }
    }
    
    /**
     * 停止HTTP服务器
     */
    public static void stop() {
        if (httpServer != null && isRunning) {
            try {
                httpServer.stop(0);
                LOGGER.info("[服务器信息API] HTTP服务器已停止");
            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 停止失败: {}", e.getMessage());
            }
            httpServer = null;
            isRunning = false;
        }
    }

    /**
     * 聊天消息监听器
     * 监听 Forge 的 ClientChatReceivedEvent 事件
     */
    @Mod.EventBusSubscriber(modid = Autosaveforforge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ChatMessageListener {
        @SubscribeEvent
        public static void onChatReceived(ClientChatReceivedEvent event) {
            Component messageComponent = event.getMessage();
            if (messageComponent == null) {
                return;
            }

            // 将 Component 转换为字符串
            String messageText = messageComponent.getString();
            if (messageText == null || messageText.trim().isEmpty()) {
                return;
            }

            LOGGER.debug("[服务器信息API] [CHAT事件] 收到原始聊天消息: {}", messageText);
            
            // 过滤系统消息（如"加入了游戏"、"离开服务器"等）
            if (isSystemMessage(messageText)) {
                LOGGER.debug("[服务器信息API] [CHAT事件] 过滤系统消息: {}", messageText);
                return;
            }
            
            // 去掉 [System] [CHAT] 或 [Not Secure] [CHAT] 前缀（如果存在）
            String cleanMessage = removeSystemPrefix(messageText);
            
            // 如果清理后的消息为空，跳过
            if (cleanMessage == null || cleanMessage.trim().isEmpty()) {
                LOGGER.debug("[服务器信息API] [CHAT事件] 清理后消息为空，跳过");
                return;
            }
            
            // 只匹配原版玩家聊天消息格式：<玩家名> 消息内容
            String playerChatMessage = extractPlayerChatMessage(cleanMessage);
            if (playerChatMessage != null && !playerChatMessage.trim().isEmpty()) {
                chatMessageQueue.offer(playerChatMessage.trim());
                LOGGER.info("[服务器信息API] [CHAT事件] 玩家聊天消息已加入队列: {}", playerChatMessage.trim());
            } else {
                LOGGER.debug("[服务器信息API] [CHAT事件] 不是原版玩家聊天消息格式，跳过: {}", cleanMessage);
            }
        }
    }
    
    /**
     * 判断是否是系统消息（需要过滤掉的消息）
     * @param message 消息内容
     * @return true表示是系统消息，需要过滤
     */
    private static boolean isSystemMessage(String message) {
        if (message == null) {
            return true;
        }
        
        String lower = message.toLowerCase();
        
        // 过滤系统提示消息
        return lower.contains("加入了游戏") ||
               lower.contains("离开服务器") ||
               lower.contains("进入了服务器") ||
               lower.contains("退出了服务器") ||
               lower.contains("joined the game") ||
               lower.contains("left the game");
    }
    
    /**
     * 判断消息是否包含方括号格式（用于识别自定义消息格式）
     * @param message 消息内容
     * @return true表示消息中包含至少一个方括号块，可能是自定义格式
     */
    private static boolean hasBracketFormat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = message.trim();
        // 检查是否包含至少一个完整的方括号块 [xxx]
        Pattern bracketPattern = Pattern.compile("\\[[^\\]]+\\]");
        return bracketPattern.matcher(trimmed).find();
    }
    
    /**
     * 提取原版玩家聊天消息
     * 只匹配格式：<玩家名> 消息内容
     * @param message 清理后的消息
     * @return 如果是原版玩家聊天消息，返回完整消息；否则返回null
     */
    private static String extractPlayerChatMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = message.trim();
        
        // 匹配原版玩家聊天格式：<玩家名> 消息内容
        // 玩家名可以包含字母、数字、下划线、中文字符等
        Pattern pattern = Pattern.compile(
            "^\\s*<([^>]+)>\\s*(.+)$"
        );
        
        Matcher matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            String playerName = matcher.group(1).trim();
            String chatContent = matcher.group(2).trim();
            
            // 返回完整消息（包含玩家名和消息内容）
            LOGGER.debug("[服务器信息API] ✓ 匹配原版玩家聊天格式 - <{}> {}", playerName, chatContent);
            return trimmed; // 返回完整消息
        }
        
        return null;
    }
    
    /**
     * 去掉消息中的 [System] [CHAT] 或 [Not Secure] [CHAT] 前缀
     * @param message 原始消息
     * @return 去掉前缀后的消息
     */
    private static String removeSystemPrefix(String message) {
        if (message == null) {
            return null;
        }
        
        String cleaned = message;
        
        // 匹配 [System] [CHAT] 前缀并去掉
        Pattern pattern = Pattern.compile("^\\s*\\[System\\]\\s*\\[CHAT\\]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.matches()) {
            cleaned = matcher.group(1).trim();
            LOGGER.debug("[服务器信息API] 去掉 [System] [CHAT] 前缀: {} -> {}", message, cleaned);
        }
        
        // 匹配 [Not Secure] [CHAT] 前缀并去掉
        pattern = Pattern.compile("^\\s*\\[Not Secure\\]\\s*\\[CHAT\\]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(cleaned);
        if (matcher.matches()) {
            cleaned = matcher.group(1).trim();
            LOGGER.debug("[服务器信息API] 去掉 [Not Secure] [CHAT] 前缀: {} -> {}", message, cleaned);
        }
        
        // 匹配 [System] 前缀并去掉
        pattern = Pattern.compile("^\\s*\\[System\\]\\s*(.+)$", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(cleaned);
        if (matcher.matches()) {
            cleaned = matcher.group(1).trim();
            LOGGER.debug("[服务器信息API] 去掉 [System] 前缀: {} -> {}", message, cleaned);
        }
        
        return cleaned;
    }
    
    /**
     * 解析自定义消息格式
     * 支持三种格式：
     * 1. [xxx] [xxx] [xxx] 实际消息内容（三个方括号）
     * 2. [xxx] [xxx] 实际消息内容（两个方括号）
     * 3. [xxx] 实际消息内容（一个方括号）
     * 
     * 格式示例: 
     * - [塞勒涅盟约] [雅典维亚城邦] [幸运戴师OVO] xiaofan: 114514
     * - [信息1] [信息2] 消息内容
     * - [信息] 消息内容
     * - [无头衔] MelloFurry落地过猛
     * - <xiaofanbot> 6
     * 
     * @param rawMessage 原始消息
     * @return 解析后的聊天内容（去掉方括号前缀后的实际消息），如果解析失败返回null
     */
    private static String parseCustomMessageFormat(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            LOGGER.debug("[服务器信息API] 解析消息: 输入为空");
            return null;
        }
        
        String trimmed = rawMessage.trim();
        LOGGER.debug("[服务器信息API] 开始解析消息: {}", trimmed);
        
        // 格式1: [xxx] [xxx] [xxx] 实际消息内容（三个方括号）
        Pattern pattern = Pattern.compile(
            "^\\s*\\[([^\\]]+)\\]\\s*\\[([^\\]]+)\\]\\s*\\[([^\\]]+)\\]\\s*(.+)$"
        );
        
        Matcher matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            String info1 = matcher.group(1);
            String info2 = matcher.group(2);
            String info3 = matcher.group(3);
            String chatContent = matcher.group(4).trim();
            
            LOGGER.debug("[服务器信息API] ✓ 匹配三括号格式 - [{}] [{}] [{}] -> {}", info1, info2, info3, chatContent);
            
            return chatContent;
        }
        
        // 格式2: [xxx] [xxx] 实际消息内容（两个方括号）
        pattern = Pattern.compile(
            "^\\s*\\[([^\\]]+)\\]\\s*\\[([^\\]]+)\\]\\s*(.+)$"
        );
        matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            String info1 = matcher.group(1);
            String info2 = matcher.group(2);
            String chatContent = matcher.group(3).trim();
            
            LOGGER.debug("[服务器信息API] ✓ 匹配两括号格式 - [{}] [{}] -> {}", info1, info2, chatContent);
            
            return chatContent;
        }
        
        // 格式3: [xxx] 实际消息内容（一个方括号）
        pattern = Pattern.compile(
            "^\\s*\\[([^\\]]+)\\]\\s*(.+)$"
        );
        matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            String info = matcher.group(1);
            String chatContent = matcher.group(2).trim();
            
            LOGGER.debug("[服务器信息API] ✓ 匹配单括号格式 - [{}] -> {}", info, chatContent);
            
            return chatContent;
        }
        
        // 格式4: <xxx> 消息内容（尖括号格式，如 <xiaofanbot> 6）
        pattern = Pattern.compile(
            "^\\s*<([^>]+)>\\s*(.+)$"
        );
        matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            String info = matcher.group(1);
            String chatContent = matcher.group(2).trim();
            
            LOGGER.debug("[服务器信息API] ✓ 匹配尖括号格式 - <{}> -> {}", info, chatContent);
            
            return chatContent;
        }
        
        // 如果都不匹配，返回null（不加入队列）
        LOGGER.debug("[服务器信息API] ✗ 消息格式不匹配任何模式，返回null: {}", trimmed);
        return null;
    }

    /**
     * 服务器信息处理器
     */
    private static class ServerInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理GET请求
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                Minecraft mc = Minecraft.getInstance();
                
                // 检查客户端是否连接到服务器
                boolean isConnected = mc != null 
                        && mc.getConnection() != null 
                        && mc.player != null;

                if (!isConnected) {
                    // 未连接状态，返回502
                    sendError(exchange, 502, "Forge client is not connected to server");
                    return;
                }

                // 获取在线玩家详细信息
                List<PlayerInfo> playerInfoList = getOnlinePlayers(mc);
                
                // 构建JSON响应
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{\"online_players\":[");
                
                for (int i = 0; i < playerInfoList.size(); i++) {
                    PlayerInfo info = playerInfoList.get(i);
                    if (i > 0) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"username\":\"").append(escapeJson(info.username)).append("\",");
                    jsonBuilder.append("\"latency\":").append(info.latency);
                    jsonBuilder.append("}");
                }
                
                jsonBuilder.append("],\"count\":").append(playerInfoList.size()).append("}");
                
                String jsonResponse = jsonBuilder.toString();

                // 发送响应
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                }

            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 处理请求时出错: {}", e.getMessage(), e);
                sendError(exchange, 500, "Internal server error");
            }
        }

        /**
         * 玩家信息数据类
         */
        private static class PlayerInfo {
            String username;
            int latency;
            
            PlayerInfo(String username, int latency) {
                this.username = username;
                this.latency = latency;
            }
        }

        /**
         * 获取在线玩家详细信息（包括用户名和延迟）
         */
        private List<PlayerInfo> getOnlinePlayers(Minecraft mc) {
            List<PlayerInfo> playerInfoList = new ArrayList<>();
            
            try {
                ClientPacketListener connection = mc.getConnection();
                if (connection != null) {
                    Collection<net.minecraft.client.multiplayer.PlayerInfo> players = connection.getOnlinePlayers();
                    if (players != null) {
                        // 遍历所有玩家条目
                        for (net.minecraft.client.multiplayer.PlayerInfo entry : players) {
                            if (entry != null) {
                                String username = "未知";
                                int latency = 0;
                                
                                try {
                                    // 获取玩家名称
                                    if (entry.getProfile() != null && entry.getProfile().getName() != null) {
                                        username = entry.getProfile().getName();
                                    } else {
                                        // 尝试从 DisplayName 获取
                                        Component displayName = entry.getTabListDisplayName();
                                        if (displayName != null) {
                                            username = displayName.getString();
                                        }
                                    }
                                    
                                    // 获取延迟（以毫秒为单位）
                                    latency = entry.getLatency();
                                    
                                } catch (Exception e) {
                                    LOGGER.error("[服务器信息API] 获取玩家信息时出错: {}", e.getMessage());
                                }
                                
                                playerInfoList.add(new PlayerInfo(username, latency));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 获取玩家列表时出错: {}", e.getMessage(), e);
            }
            
            return playerInfoList;
        }

        /**
         * 发送错误响应
         */
        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            String errorResponse = String.format("{\"error\":\"%s\"}", escapeJson(message));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, errorResponse.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes(StandardCharsets.UTF_8));
            }
        }

        /**
         * 转义JSON字符串
         */
        private String escapeJson(String str) {
            if (str == null) {
                return "";
            }
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
    
    /**
     * 最后一条消息处理器
     * 返回聊天框的最后一条消息，每次返回后清空，确保不会重复获取
     */
    private static class LastMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理GET请求
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                // 从队列中取出并移除一条消息（FIFO）
                String message = chatMessageQueue.poll();
                
                String response;
                if (message != null) {
                    // 返回消息内容
                    response = String.format("{\"message\":\"%s\"}", escapeJson(message));
                    LOGGER.debug("[服务器信息API] 返回消息: {}", message);
                } else {
                    // 队列为空，返回null
                    response = "{\"message\":null}";
                    LOGGER.debug("[服务器信息API] 队列为空，返回null");
                }

                // 发送响应
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }

            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 处理请求时出错: {}", e.getMessage(), e);
                sendError(exchange, 500, "Internal server error");
            }
        }
        
        /**
         * 发送错误响应
         */
        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            String errorResponse = String.format("{\"error\":\"%s\"}", escapeJson(message));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, errorResponse.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        /**
         * 转义JSON字符串
         */
        private String escapeJson(String str) {
            if (str == null) {
                return "";
            }
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
    
    /**
     * 发送消息到服务器处理器
     * 接收POST请求，将消息发送到Minecraft服务器
     * 注意：Forge 不需要使用 /c 命令，直接发送消息即可
     */
    private static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 只处理POST请求
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                // 读取请求体
                InputStream requestBody = exchange.getRequestBody();
                String requestText = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                
                LOGGER.debug("[服务器信息API] 收到发送消息请求: {}", requestText);
                
                // 手动解析JSON（避免外部依赖）
                String qqId = parseJsonString(requestText, "qq_id", "");
                String message = parseJsonString(requestText, "message", "");
                String source = parseJsonString(requestText, "source", "qq"); // 默认为qq，支持kook
                
                // 验证参数
                if (message == null || message.trim().isEmpty()) {
                    sendError(exchange, 400, "message字段不能为空");
                    return;
                }
                
                // 发送消息到Minecraft服务器（Forge 直接发送，不需要 /c 命令）
                boolean success = sendMessageToServer(qqId, message, source);
                
                if (success) {
                    String response = String.format("{\"status\":\"success\",\"message\":\"消息已发送\",\"qq_id\":\"%s\",\"source\":\"%s\"}", escapeJson(qqId), escapeJson(source));
                    sendResponse(exchange, 200, response);
                    String sourceLabel = "kook".equalsIgnoreCase(source) ? "KOOK" : "QQ";
                    LOGGER.info("[服务器信息API] 消息已发送: {}消息：用户：{}: {} ({}: {})", sourceLabel, qqId, message, sourceLabel, qqId);
                } else {
                    String response = "{\"status\":\"error\",\"message\":\"客户端未连接到服务器\"}";
                    sendResponse(exchange, 502, response);
                    LOGGER.warn("[服务器信息API] 发送失败: 客户端未连接");
                }

            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 处理请求时出错: {}", e.getMessage(), e);
                sendError(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
        
        /**
         * 发送消息到Minecraft服务器
         * @param qqId 用户昵称（作为qq_id）
         * @param message 消息内容
         * @param source 消息来源（"qq" 或 "kook"）
         * @return 是否发送成功
         */
        private boolean sendMessageToServer(String qqId, String message, String source) {
            Minecraft mc = Minecraft.getInstance();
            
            // 检查客户端是否连接到服务器
            if (mc == null || mc.getConnection() == null || mc.player == null) {
                return false;
            }
            
            try {
                // 根据来源构建不同的消息格式
                String finalMessage;
                if ("kook".equalsIgnoreCase(source)) {
                    // KOOK消息格式：KOOK消息：用户：用户名: 消息内容
                    finalMessage = "KOOK消息：用户：" + qqId + ": " + message;
                } else {
                    // QQ消息格式：QQ消息：用户：用户名: 消息内容
                    finalMessage = "QQ消息：用户：" + qqId + ": " + message;
                }
                
                // 在主游戏线程中执行（Forge 直接发送消息，不需要 /c 命令）
                mc.execute(() -> {
                    try {
                        // 直接发送消息（不需要 /c 命令）
                        // Forge 1.20.1 使用 connection.sendChat() 方法，接受 String 参数
                        if (mc.player != null && mc.getConnection() != null) {
                            mc.getConnection().sendChat(finalMessage);
                            LOGGER.debug("[服务器信息API] 已发送消息: {}", finalMessage);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[服务器信息API] 发送消息时出错: {}", e.getMessage(), e);
                    }
                });
                
                return true;
            } catch (Exception e) {
                LOGGER.error("[服务器信息API] 执行消息发送时出错: {}", e.getMessage(), e);
                return false;
            }
        }
        
        /**
         * 发送成功响应
         */
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        /**
         * 发送错误响应
         */
        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            String errorResponse = String.format("{\"error\":\"%s\"}", escapeJson(message));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, errorResponse.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        /**
         * 手动解析JSON字符串中的字段值
         * @param json JSON字符串
         * @param key 要获取的键
         * @param defaultValue 默认值
         * @return 字段值，如果不存在则返回默认值
         */
        private String parseJsonString(String json, String key, String defaultValue) {
            if (json == null || json.trim().isEmpty()) {
                return defaultValue;
            }
            
            // 查找 "key": "value" 或 "key": "value"
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            
            if (m.find()) {
                return m.group(1);
            }
            
            // 如果没有找到，返回默认值
            return defaultValue;
        }

        /**
         * 转义JSON字符串
         */
        private String escapeJson(String str) {
            if (str == null) {
                return "";
            }
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    /**
     * 调试界面处理器
     * 提供 Web 界面用于测试 /send_message_to_server 接口
     */
    private static class DebugHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String html = generateDebugHTML();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }

        private String generateDebugHTML() {
            return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>服务器信息 API 调试界面</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Consolas', 'Monaco', monospace;
                        background: #1e1e1e;
                        color: #d4d4d4;
                        padding: 20px;
                    }
                    .container {
                        max-width: 1000px;
                        margin: 0 auto;
                    }
                    h1 {
                        color: #4ec9b0;
                        margin-bottom: 20px;
                        border-bottom: 2px solid #3e3e42;
                        padding-bottom: 10px;
                    }
                    .card {
                        background: #252526;
                        border: 1px solid #3e3e42;
                        border-radius: 8px;
                        padding: 20px;
                        margin-bottom: 20px;
                    }
                    .card-title {
                        color: #4ec9b0;
                        font-size: 1.2em;
                        margin-bottom: 15px;
                    }
                    .form-group {
                        margin-bottom: 15px;
                    }
                    label {
                        display: block;
                        color: #d4d4d4;
                        margin-bottom: 5px;
                    }
                    input[type="text"],
                    select {
                        width: 100%;
                        padding: 8px;
                        background: #1e1e1e;
                        border: 1px solid #3e3e42;
                        color: #d4d4d4;
                        border-radius: 4px;
                        font-family: inherit;
                    }
                    input[type="text"]:focus,
                    select:focus {
                        outline: none;
                        border-color: #4ec9b0;
                    }
                    button {
                        padding: 10px 20px;
                        background: #569cd6;
                        color: #fff;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 1em;
                        margin-right: 10px;
                    }
                    button:hover {
                        background: #4a8cd2;
                    }
                    button:disabled {
                        background: #3e3e42;
                        cursor: not-allowed;
                    }
                    .response-area {
                        background: #1e1e1e;
                        border: 1px solid #3e3e42;
                        border-radius: 4px;
                        padding: 15px;
                        margin-top: 15px;
                        min-height: 100px;
                        font-family: 'Consolas', 'Monaco', monospace;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    }
                    .response-area.success {
                        border-color: #4CAF50;
                        color: #4CAF50;
                    }
                    .response-area.error {
                        border-color: #f44336;
                        color: #f44336;
                    }
                    .status-info {
                        color: #808080;
                        font-size: 0.9em;
                        margin-top: 10px;
                    }
                    .api-endpoints {
                        margin-top: 20px;
                    }
                    .endpoint {
                        background: #1e1e1e;
                        padding: 10px;
                        margin-bottom: 10px;
                        border-left: 3px solid #569cd6;
                        border-radius: 4px;
                    }
                    .endpoint-method {
                        color: #4ec9b0;
                        font-weight: bold;
                    }
                    .endpoint-path {
                        color: #dcdcaa;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔧 服务器信息 API 调试界面</h1>
                    
                    <div class="card">
                        <div class="card-title">测试 /send_message_to_server 接口</div>
                        <form id="testForm">
                            <div class="form-group">
                                <label for="qqId">QQ ID / 用户名:</label>
                                <input type="text" id="qqId" name="qqId" value="test_user" placeholder="输入用户ID">
                            </div>
                            <div class="form-group">
                                <label for="message">消息内容:</label>
                                <input type="text" id="message" name="message" value="测试消息" placeholder="输入要发送的消息">
                            </div>
                            <div class="form-group">
                                <label for="source">消息来源:</label>
                                <select id="source" name="source">
                                    <option value="qq" selected>QQ</option>
                                    <option value="kook">KOOK</option>
                                </select>
                            </div>
                            <button type="button" onclick="testSendMessage()">📤 发送测试消息</button>
                            <button type="button" onclick="checkConnectionStatus()">🔍 检查连接状态</button>
                        </form>
                        <div id="responseArea" class="response-area">等待操作...</div>
                        <div class="status-info" id="statusInfo"></div>
                    </div>

                    <div class="card api-endpoints">
                        <div class="card-title">可用 API 端点</div>
                        <div class="endpoint">
                            <span class="endpoint-method">GET</span>
                            <span class="endpoint-path"> /need_server_info</span>
                            <div style="color: #808080; margin-top: 5px;">获取在线玩家信息</div>
                        </div>
                        <div class="endpoint">
                            <span class="endpoint-method">GET</span>
                            <span class="endpoint-path"> /get_server_last_message</span>
                            <div style="color: #808080; margin-top: 5px;">获取最后一条聊天消息</div>
                        </div>
                        <div class="endpoint">
                            <span class="endpoint-method">POST</span>
                            <span class="endpoint-path"> /send_message_to_server</span>
                            <div style="color: #808080; margin-top: 5px;">发送消息到服务器</div>
                        </div>
                        <div class="endpoint">
                            <span class="endpoint-method">GET</span>
                            <span class="endpoint-path"> /debug</span>
                            <div style="color: #808080; margin-top: 5px;">调试界面（当前页面）</div>
                        </div>
                    </div>
                </div>

                <script>
                    async function testSendMessage() {
                        const qqId = document.getElementById('qqId').value.trim();
                        const message = document.getElementById('message').value.trim();
                        const source = document.getElementById('source').value;
                        const responseArea = document.getElementById('responseArea');
                        const statusInfo = document.getElementById('statusInfo');

                        if (!message) {
                            responseArea.className = 'response-area error';
                            responseArea.textContent = '错误: 消息内容不能为空';
                            return;
                        }

                        responseArea.className = 'response-area';
                        responseArea.textContent = '正在发送请求...';
                        statusInfo.textContent = '';

                        try {
                            const requestBody = {
                                qq_id: qqId || 'test_user',
                                message: message,
                                source: source
                            };

                            const response = await fetch('/send_message_to_server', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json; charset=utf-8'
                                },
                                body: JSON.stringify(requestBody)
                            });

                            const responseText = await response.text();
                            let responseData;
                            try {
                                responseData = JSON.parse(responseText);
                            } catch (e) {
                                responseData = { raw: responseText };
                            }

                            if (response.ok) {
                                responseArea.className = 'response-area success';
                                responseArea.textContent = '✅ 请求成功\\n\\n状态码: ' + response.status + '\\n\\n响应数据:\\n' + JSON.stringify(responseData, null, 2);
                                statusInfo.textContent = '消息已发送到游戏服务器';
                            } else {
                                responseArea.className = 'response-area error';
                                responseArea.textContent = '❌ 请求失败\\n\\n状态码: ' + response.status + '\\n\\n响应数据:\\n' + JSON.stringify(responseData, null, 2);
                                statusInfo.textContent = '发送失败，请检查游戏是否已连接到服务器';
                            }
                        } catch (error) {
                            responseArea.className = 'response-area error';
                            responseArea.textContent = '❌ 请求异常\\n\\n错误信息: ' + error.message;
                            statusInfo.textContent = '网络错误或服务器未响应';
                        }
                    }

                    async function checkConnectionStatus() {
                        const responseArea = document.getElementById('responseArea');
                        const statusInfo = document.getElementById('statusInfo');

                        responseArea.className = 'response-area';
                        responseArea.textContent = '正在检查连接状态...';
                        statusInfo.textContent = '';

                        try {
                            const response = await fetch('/need_server_info');
                            const data = await response.json();

                            if (response.ok) {
                                responseArea.className = 'response-area success';
                                responseArea.textContent = '✅ 连接正常\\n\\n在线玩家数: ' + data.count + '\\n\\n玩家列表:\\n' + JSON.stringify(data.online_players, null, 2);
                                statusInfo.textContent = '游戏已连接到服务器';
                            } else {
                                responseArea.className = 'response-area error';
                                responseArea.textContent = '❌ 未连接到服务器\\n\\n状态码: ' + response.status + '\\n\\n响应: ' + JSON.stringify(data, null, 2);
                                statusInfo.textContent = '请确保游戏已连接到服务器';
                            }
                        } catch (error) {
                            responseArea.className = 'response-area error';
                            responseArea.textContent = '❌ 检查失败\\n\\n错误信息: ' + error.message;
                            statusInfo.textContent = '无法连接到 API 服务器';
                        }
                    }

                    // 回车键发送
                    document.getElementById('message').addEventListener('keypress', function(e) {
                        if (e.key === 'Enter') {
                            testSendMessage();
                        }
                    });
                </script>
            </body>
            </html>
            """;
        }

        private void sendError(HttpExchange exchange, int code, String message) throws IOException {
            String response = "{\"error\":\"" + escapeJson(message) + "\",\"code\":" + code + "}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }

        private String escapeJson(String str) {
            if (str == null) {
                return "";
            }
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}

