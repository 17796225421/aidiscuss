package com.example.service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GptService {
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时时间
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时时间
            .readTimeout(120, TimeUnit.SECONDS) // 读取超时时间
            .build();
    private Dotenv dotenv = Dotenv.configure().load();
    private final String gpt4Key = dotenv.get("GPT4_KEY");
    private final String gpt4Url = dotenv.get("GPT4_URL");
    private final String gpt3Key = dotenv.get("GPT3_KEY");
    private final String gpt3Url = dotenv.get("GPT3_URL");


    public String requestGpt3(String model, String system, String user) throws IOException {
        // 构建JSON请求体
        JsonObject jsonObject = buildJsonRequestBody(model, system, user, false);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        // 构建请求
        Request request = new Request.Builder()
                .url(gpt3Url)
                .addHeader("Authorization", "Bearer " + gpt3Key)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            // 获取GPT文本
            String gptContent = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            return gptContent;
        }
    }

    public String requestGpt4(String model, String system, String user) throws IOException {
        return requestGpt4(model, system, user, false);
    }

    public String requestGpt4(String model, String system, String user, boolean stream) throws IOException {
        // 构建JSON请求体
        JsonObject jsonObject = buildJsonRequestBody(model, system, user, stream);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        // 构建请求
        Request request = new Request.Builder()
                .url(gpt4Url)
                .addHeader("Authorization", "Bearer " + gpt4Key)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            // 获取GPT文本
            String gptContent = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            return gptContent;
        }
    }

    private JsonObject buildJsonRequestBody(String model, String system, String user, boolean stream) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", model);
        jsonObject.addProperty("stream", stream);
        jsonObject.addProperty("max_tokens", 4096);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "system");
        message.addProperty("content", system);
        messages.add(message);

        message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", user);
        messages.add(message);

        jsonObject.add("messages", messages);
        return jsonObject;
    }

    public static void main(String[] args) throws IOException {
        GptService gptService = new GptService();
        String s = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是个有帮助的助手", "早上吃什么");
        System.out.println(s);
    }

}
