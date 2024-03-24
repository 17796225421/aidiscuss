package com.example.thread;


import com.example.model.Cursor;
import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimeSlicedSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true; // 添加 running 标志变量
    private RedisService redisService = RedisService.getInstance();
    private GptService gptService = new GptService();

    public TimeSlicedSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // 周期一分钟
                Thread.sleep(10 * 1000);
                if (!running) {
                    break;
                }

                // 从Redis中获取三种类型的MicSentences数据
                String externMicSentencesJson = redisService.getExternMicSentences(discussId);
                String wireMicSentencesJson = redisService.getWireMicSentences(discussId);
                String virtualMicSentencesJson = redisService.getVirtualMicSentences(discussId);

                // 解析JSON字符串中的queue数组
                List<Sentence> externMicSentences = parseSentencesFromQueue(externMicSentencesJson);
                List<Sentence> wireMicSentences = parseSentencesFromQueue(wireMicSentencesJson);
                List<Sentence> virtualMicSentences = parseSentencesFromQueue(virtualMicSentencesJson);


                Cursor timeSlicedSummaryCursor = redisService.getTimeSlicedSummaryCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                // 遍历三个列表，从游标开始积累unprocessedText
                while (timeSlicedSummaryCursor.getExternCursor() < externMicSentences.size() || timeSlicedSummaryCursor.getWireCursor() < wireMicSentences.size() || timeSlicedSummaryCursor.getVirtualCursor() < virtualMicSentences.size()) {

                    // 创建一个临时列表存放当前游标指向的句子
                    List<Sentence> currentSentences = new ArrayList<>();

                    if (timeSlicedSummaryCursor.getExternCursor() < externMicSentences.size()) {
                        currentSentences.add(externMicSentences.get(timeSlicedSummaryCursor.getExternCursor()));
                    }
                    if (timeSlicedSummaryCursor.getWireCursor() < wireMicSentences.size()) {
                        currentSentences.add(wireMicSentences.get(timeSlicedSummaryCursor.getWireCursor()));
                    }
                    if (timeSlicedSummaryCursor.getVirtualCursor() < virtualMicSentences.size()) {
                        currentSentences.add(virtualMicSentences.get(timeSlicedSummaryCursor.getVirtualCursor()));
                    }

                    // 根据句子的开始时间排序，取出时间最小的句子
                    Collections.sort(currentSentences, Comparator.comparing(Sentence::getBeginTime));
                    Sentence earliestSentence = currentSentences.get(0);

                    // 将时间最小的句子的文本添加到unprocessedText
                    unprocessedText.append(earliestSentence.getText());

                    // 更新对应游标的位置
                    if (timeSlicedSummaryCursor.getExternCursor() < externMicSentences.size() && earliestSentence == externMicSentences.get(timeSlicedSummaryCursor.getExternCursor())) {
                        timeSlicedSummaryCursor.setExternCursor(timeSlicedSummaryCursor.getExternCursor() + 1);
                    } else if (timeSlicedSummaryCursor.getWireCursor() < wireMicSentences.size() && earliestSentence == wireMicSentences.get(timeSlicedSummaryCursor.getWireCursor())) {
                        timeSlicedSummaryCursor.setWireCursor(timeSlicedSummaryCursor.getWireCursor() + 1);
                    } else if (timeSlicedSummaryCursor.getVirtualCursor() < virtualMicSentences.size() && earliestSentence == virtualMicSentences.get(timeSlicedSummaryCursor.getVirtualCursor())) {
                        timeSlicedSummaryCursor.setVirtualCursor(timeSlicedSummaryCursor.getVirtualCursor() + 1);
                    }
                }

                if (unprocessedText.length() > 0) {
                    String text = unprocessedText.toString();
                    String segmentSummary = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", text);
                    System.out.println("segmentSummary" + segmentSummary);
                    redisService.addSegmentSummary(discussId, segmentSummary);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setTimeSlicedSummaryCursor(discussId, timeSlicedSummaryCursor);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
