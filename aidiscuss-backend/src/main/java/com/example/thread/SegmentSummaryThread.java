package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SegmentSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = RedisService.getInstance();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 10;

    public SegmentSummaryThread(String discussId) {
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
                List<Sentence> externMicSentences = parseSentencesFromQueue(externMicSentencesJson);
                List<Sentence> wireMicSentences = parseSentencesFromQueue(wireMicSentencesJson);
                List<Sentence> virtualMicSentences = parseSentencesFromQueue(virtualMicSentencesJson);


                // 从Redis中获取三个游标
                int externCursor = redisService.getExternCursor(discussId);
                int wireCursor = redisService.getWireCursor(discussId);
                int virtualCursor = redisService.getVirtualCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                // 遍历三个列表，从游标开始积累unprocessedText
                while (externCursor < externMicSentences.size() ||
                        wireCursor < wireMicSentences.size() ||
                        virtualCursor < virtualMicSentences.size()) {

                    // 创建一个临时列表存放当前游标指向的句子
                    List<Sentence> currentSentences = new ArrayList<>();

                    if (externCursor < externMicSentences.size()) {
                        currentSentences.add(externMicSentences.get(externCursor));
                    }
                    if (wireCursor < wireMicSentences.size()) {
                        currentSentences.add(wireMicSentences.get(wireCursor));
                    }
                    if (virtualCursor < virtualMicSentences.size()) {
                        currentSentences.add(virtualMicSentences.get(virtualCursor));
                    }

                    // 根据句子的开始时间排序，取出时间最小的句子
                    Collections.sort(currentSentences, Comparator.comparing(Sentence::getBeginTime));
                    Sentence earliestSentence = currentSentences.get(0);

                    // 将时间最小的句子的文本添加到unprocessedText
                    unprocessedText.append(earliestSentence.getText());

                    // 更新对应游标的位置
                    if (externCursor < externMicSentences.size() && earliestSentence == externMicSentences.get(externCursor)) {
                        externCursor++;
                    } else if (wireCursor < wireMicSentences.size() && earliestSentence == wireMicSentences.get(wireCursor)) {
                        wireCursor++;
                    } else if (virtualCursor < virtualMicSentences.size() && earliestSentence == virtualMicSentences.get(virtualCursor)) {
                        virtualCursor++;
                    }
                }

                // 如果未处理的text长度超过300,则提取出来调用requestGpt4方法进行处理
                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentSummary = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", text);
                    System.out.println("segmentSummary" + segmentSummary);
                    redisService.addSegmentSummary(discussId,segmentSummary);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setExternCursor(discussId, externCursor);
                    redisService.setWireCursor(discussId, wireCursor);
                    redisService.setVirtualCursor(discussId, virtualCursor);
                }

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        running = false;
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
    private List<Sentence> parseSentencesFromQueue(String json) {
        // 使用Gson库解析JSON字符串
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        // 从JsonObject中获取名为queue的JsonArray
        String queueJson = jsonObject.getAsJsonArray("queue").toString();
        // 将JsonArray字符串转换为Sentence列表
        return gson.fromJson(queueJson, new TypeToken<List<Sentence>>() {
        }.getType());
    }
}