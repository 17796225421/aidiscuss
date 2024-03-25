package com.example.thread;

import com.example.model.Cursor;
import com.example.model.Sentencetmp;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class KeyWordThread extends Thread {
    private String discussId;
    private volatile boolean running = true; // 添加 running 标志变量
    private RedisService redisService = RedisService.getInstance();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 10;
    public KeyWordThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // 从Redis中获取三种类型的MicSentences数据
                String externMicSentencesJson = redisService.getExternMicSentences(discussId);
                String wireMicSentencesJson = redisService.getWireMicSentences(discussId);
                String virtualMicSentencesJson = redisService.getVirtualMicSentences(discussId);

                // 解析JSON字符串中的queue数组
                List<Sentencetmp> externMicSentencetmps = parseSentencesFromQueue(externMicSentencesJson);
                List<Sentencetmp> wireMicSentencetmps = parseSentencesFromQueue(wireMicSentencesJson);
                List<Sentencetmp> virtualMicSentencetmps = parseSentencesFromQueue(virtualMicSentencesJson);


                Cursor keyWordCursor = redisService.getKeyWordCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                // 遍历三个列表，从游标开始积累unprocessedText
                while (keyWordCursor.getExternCursor() < externMicSentencetmps.size() || keyWordCursor.getWireCursor() < wireMicSentencetmps.size() || keyWordCursor.getVirtualCursor() < virtualMicSentencetmps.size()) {

                    // 创建一个临时列表存放当前游标指向的句子
                    List<Sentencetmp> currentSentencetmps = new ArrayList<>();

                    if (keyWordCursor.getExternCursor() < externMicSentencetmps.size()) {
                        currentSentencetmps.add(externMicSentencetmps.get(keyWordCursor.getExternCursor()));
                    }
                    if (keyWordCursor.getWireCursor() < wireMicSentencetmps.size()) {
                        currentSentencetmps.add(wireMicSentencetmps.get(keyWordCursor.getWireCursor()));
                    }
                    if (keyWordCursor.getVirtualCursor() < virtualMicSentencetmps.size()) {
                        currentSentencetmps.add(virtualMicSentencetmps.get(keyWordCursor.getVirtualCursor()));
                    }

                    // 根据句子的开始时间排序，取出时间最小的句子
                    Collections.sort(currentSentencetmps, Comparator.comparing(Sentencetmp::getBeginTime));
                    Sentencetmp earliestSentencetmp = currentSentencetmps.get(0);

                    // 将时间最小的句子的文本添加到unprocessedText
                    unprocessedText.append(earliestSentencetmp.getText());

                    // 更新对应游标的位置
                    if (keyWordCursor.getExternCursor() < externMicSentencetmps.size() && earliestSentencetmp == externMicSentencetmps.get(keyWordCursor.getExternCursor())) {
                        keyWordCursor.setExternCursor(keyWordCursor.getExternCursor() + 1);
                    } else if (keyWordCursor.getWireCursor() < wireMicSentencetmps.size() && earliestSentencetmp == wireMicSentencetmps.get(keyWordCursor.getWireCursor())) {
                        keyWordCursor.setWireCursor(keyWordCursor.getWireCursor() + 1);
                    } else if (keyWordCursor.getVirtualCursor() < virtualMicSentencetmps.size() && earliestSentencetmp == virtualMicSentencetmps.get(keyWordCursor.getVirtualCursor())) {
                        keyWordCursor.setVirtualCursor(keyWordCursor.getVirtualCursor() + 1);
                    }
                }

                // 如果未处理的text长度超过300,则提取出来调用requestGpt4方法进行处理
                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String gptText = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", text);
                    List<String> keyWordList = Arrays.asList(gptText.split("\n"));

                    redisService.addKeyWordList(discussId, keyWordList);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setKeyWordCursor(discussId, keyWordCursor);
                }

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        running = false; // 设置 running 为 false,通知麦克风线程停止运行
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 解析JSON字符串中的queue数组，并将其转换为Sentence列表
     *
     * @param json JSON字符串
     * @return Sentence列表
     */
    private List<Sentencetmp> parseSentencesFromQueue(String json) {
        // 使用Gson库解析JSON字符串
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        // 从JsonObject中获取名为queue的JsonArray
        String queueJson = jsonObject.getAsJsonArray("queue").toString();
        // 将JsonArray字符串转换为Sentence列表
        return gson.fromJson(queueJson, new TypeToken<List<Sentencetmp>>() {
        }.getType());
    }
}
