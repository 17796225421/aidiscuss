package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SegmentMultiangleThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentMultiangleThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentMultiangleCursor = redisService.getSegmentMultiangleCursor(discussId);
                List<String> backgroundList = redisService.getBackgroundList(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentMultiangleCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentMultiangleCursor).getText());
                    segmentMultiangleCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentMultiangle = gptService.requestQwen("qwen1.5-110b-chat", "你是一个思维发散头脑灵活知识广度大的程序员",
                            "背景信息：" + backgroundList.toString() + "\n背景信息结束\n" +
                            "从多角度分析以下内容：" + text);
//                    System.out.println("segmentMultiangle" + segmentMultiangle);
                    redisService.addSegmentMultiangle(discussId, segmentMultiangle);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentMultiangleCursor(discussId, segmentMultiangleCursor);
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