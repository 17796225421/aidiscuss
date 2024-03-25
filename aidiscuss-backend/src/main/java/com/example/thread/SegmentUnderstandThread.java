package com.example.thread;

import com.example.model.Cursor;
import com.example.model.Sentencetmp;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SegmentUnderstandThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = RedisService.getInstance();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 10;

    public SegmentUnderstandThread(String discussId) {
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


                Cursor segmentUnderstandCursor = redisService.getSegmentUnderstandCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                // 遍历三个列表，从游标开始积累unprocessedText
                while (segmentUnderstandCursor.getExternCursor() < externMicSentencetmps.size() || segmentUnderstandCursor.getWireCursor() < wireMicSentencetmps.size() || segmentUnderstandCursor.getVirtualCursor() < virtualMicSentencetmps.size()) {

                    // 创建一个临时列表存放当前游标指向的句子
                    List<Sentencetmp> currentSentencetmps = new ArrayList<>();

                    if (segmentUnderstandCursor.getExternCursor() < externMicSentencetmps.size()) {
                        currentSentencetmps.add(externMicSentencetmps.get(segmentUnderstandCursor.getExternCursor()));
                    }
                    if (segmentUnderstandCursor.getWireCursor() < wireMicSentencetmps.size()) {
                        currentSentencetmps.add(wireMicSentencetmps.get(segmentUnderstandCursor.getWireCursor()));
                    }
                    if (segmentUnderstandCursor.getVirtualCursor() < virtualMicSentencetmps.size()) {
                        currentSentencetmps.add(virtualMicSentencetmps.get(segmentUnderstandCursor.getVirtualCursor()));
                    }

                    // 根据句子的开始时间排序，取出时间最小的句子
                    Collections.sort(currentSentencetmps, Comparator.comparing(Sentencetmp::getBeginTime));
                    Sentencetmp earliestSentencetmp = currentSentencetmps.get(0);

                    // 将时间最小的句子的文本添加到unprocessedText
                    unprocessedText.append(earliestSentencetmp.getText());

                    // 更新对应游标的位置
                    if (segmentUnderstandCursor.getExternCursor() < externMicSentencetmps.size() && earliestSentencetmp == externMicSentencetmps.get(segmentUnderstandCursor.getExternCursor())) {
                        segmentUnderstandCursor.setExternCursor(segmentUnderstandCursor.getExternCursor() + 1);
                    } else if (segmentUnderstandCursor.getWireCursor() < wireMicSentencetmps.size() && earliestSentencetmp == wireMicSentencetmps.get(segmentUnderstandCursor.getWireCursor())) {
                        segmentUnderstandCursor.setWireCursor(segmentUnderstandCursor.getWireCursor() + 1);
                    } else if (segmentUnderstandCursor.getVirtualCursor() < virtualMicSentencetmps.size() && earliestSentencetmp == virtualMicSentencetmps.get(segmentUnderstandCursor.getVirtualCursor())) {
                        segmentUnderstandCursor.setVirtualCursor(segmentUnderstandCursor.getVirtualCursor() + 1);
                    }
                }

                // 如果未处理的text长度超过300,则提取出来调用requestGpt4方法进行处理
                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentUnderstand = gptService.requestGpt3("gpt-3.5-turbo-0125", "分析下面这段话的潜在意图、隐含信息、额外背景", text);
                    System.out.println("333" + segmentUnderstand);
                    redisService.addSegmentUnderstand(discussId, segmentUnderstand);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentUnderstandCursor(discussId, segmentUnderstandCursor);
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