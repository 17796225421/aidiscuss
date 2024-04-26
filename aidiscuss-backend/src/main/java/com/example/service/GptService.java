package com.example.service;

import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class GptService {
    TranslateService translateService = new TranslateService();
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
    private final String llama3Key = dotenv.get("LLAMA3_KEY");
    private final String llama3Url = dotenv.get("LLAMA3_URL");

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
        // 构建JSON请求体
        JsonObject jsonObject = buildJsonRequestBody(model, system, user, false);
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

    public String requestLlama3(String model, String system, String chineseUser) throws IOException {
        String user = translateService.translateToEnglish(chineseUser);
        // 构建JSON请求体
        JsonObject jsonObject = buildJsonRequestBody(model, system, user, false);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        OkHttpClient clientWithProxy = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)))
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(llama3Url)
                .addHeader("Authorization", "Bearer " + llama3Key)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = clientWithProxy.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            String englishAnswer = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            return translateService.translateToChinese(englishAnswer);
        }
    }


    public BufferedSource requestGpt4Stream(String model, String system, String user) throws IOException {
        JsonObject jsonObject = buildJsonRequestBody(model, system, user, true);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder()
                .url(gpt4Url)
                .addHeader("Authorization", "Bearer " + gpt4Key)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        return response.body().source();
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
        StringBuilder accumulativeContent = new StringBuilder();
        String lastContent = "";
        long lastPrintTime = 0;

        try {
            BufferedSource source = gptService.requestGpt4Stream("gpt-4-turbo-2024-04-09", "你是个有帮助的助手", "晚上吃什么");
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line != null && !line.isEmpty()) {
                    if (line.startsWith("data:")) {
                        String jsonStr = line.substring("data:".length()).trim();
                        if (jsonStr.equals("[DONE]")) {
                            break;
                        }
                        JsonObject data = JsonParser.parseString(jsonStr).getAsJsonObject();
                        JsonArray choices = data.getAsJsonArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").getAsString();
                                if (content != null) {
                                    accumulativeContent.append(content);
                                    long currentTime = System.currentTimeMillis();
                                    if (!accumulativeContent.toString().equals(lastContent) && currentTime - lastPrintTime >= 1000) {
                                        lastContent = accumulativeContent.toString();
                                        System.out.println(lastContent);
                                        lastPrintTime = currentTime;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println(accumulativeContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
