package com.xiaofan.fanmacro.gui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * AI 客户端 - 与 AI 服务器通信
 */
public class AIClient {
    private static final String SERVER_URL = "http://211.101.244.28:54046";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Gson gson = new Gson();
    
    /**
     * 提交需求到服务器
     * @param userText 用户需求描述
     * @param onSuccess 成功回调，参数为 takeid
     * @param onError 错误回调，参数为错误消息
     */
    public static void submitRequest(String userText, Consumer<String> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                // 构建请求体
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("IP", "");
                requestBody.addProperty("text", userText);
                
                RequestBody body = RequestBody.create(
                    gson.toJson(requestBody),
                    MediaType.parse("application/json; charset=utf-8")
                );
                
                Request request = new Request.Builder()
                    .url(SERVER_URL + "/input")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        onError.accept("服务器错误: " + response.code() + " " + response.message());
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                    
                    if (responseJson.has("code") && responseJson.get("code").getAsInt() == 200) {
                        String takeid = responseJson.get("takeid").getAsString();
                        onSuccess.accept(takeid);
                    } else {
                        onError.accept("服务器返回错误: " + responseBody);
                    }
                }
            } catch (IOException e) {
                onError.accept("网络错误: " + e.getMessage());
            } catch (Exception e) {
                onError.accept("请求失败: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 查询生成结果
     * @param takeid 任务ID
     * @param onSuccess 成功回调，参数为生成的代码（如果还在生成中，参数为 null）
     * @param onError 错误回调，参数为错误消息
     */
    public static void queryResult(String takeid, Consumer<String> onSuccess, Consumer<String> onError) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                    .url(SERVER_URL + "/output?takeid=" + takeid)
                    .get()
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        onError.accept("服务器错误: " + response.code() + " " + response.message());
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    
                    if ("wait".equals(responseBody.trim())) {
                        // 还在生成中
                        onSuccess.accept(null);
                    } else {
                        // 生成完成
                        onSuccess.accept(responseBody);
                    }
                }
            } catch (IOException e) {
                onError.accept("网络错误: " + e.getMessage());
            } catch (Exception e) {
                onError.accept("查询失败: " + e.getMessage());
            }
        }).start();
    }
}

