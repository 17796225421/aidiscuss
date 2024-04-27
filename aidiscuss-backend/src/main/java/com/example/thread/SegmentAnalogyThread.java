package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SegmentAnalogyThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentAnalogyThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentAnalogyCursor = redisService.getSegmentAnalogyCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentAnalogyCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentAnalogyCursor).getText());
                    segmentAnalogyCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentAnalogy = gptService.requestLlama3("llama3-70b-8192", "你是一个有帮助的助手", text);
//                    System.out.println("segmentAnalogy" + segmentAnalogy);
                    redisService.addSegmentAnalogy(discussId, segmentAnalogy);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentAnalogyCursor(discussId, segmentAnalogyCursor);
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