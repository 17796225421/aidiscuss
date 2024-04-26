package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SegmentUnderstandThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentUnderstandThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentUnderstandCursor = redisService.getSegmentUnderstandCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentUnderstandCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentUnderstandCursor).getText());
                    segmentUnderstandCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentUnderstand = gptService.requestLlama3("llama3-70b-8192", "你是一个擅长提供潜在意图隐藏信息额外背景来辅助理解一大段文字的老师", "对于以下文字，输出辅助理解的潜在意图隐藏信息额外背景\n" + text);
                    System.out.println("segmentUnderstand" + segmentUnderstand);
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

}