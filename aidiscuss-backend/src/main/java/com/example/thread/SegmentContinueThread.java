package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SegmentContinueThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentContinueThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentContinueCursor = redisService.getSegmentContinueCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentContinueCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentContinueCursor).getText());
                    segmentContinueCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentContinue = gptService.requestLlama3("llama3-70b-8192", "你擅长根据根据已有信息推理之后会发生的事情", text+"\n之后会发生什么？");
//                    System.out.println("segmentContinue" + segmentContinue);
                    redisService.addSegmentContinue(discussId, segmentContinue);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentContinueCursor(discussId, segmentContinueCursor);
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