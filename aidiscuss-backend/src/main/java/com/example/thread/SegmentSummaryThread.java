package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SegmentSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentSummaryCursor = redisService.getSegmentSummaryCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentSummaryCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentSummaryCursor).getText());
                    segmentSummaryCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
//                    String segmentSummary = gptService.requestGpt3("gpt-3.5-turbo", "你是一个有帮助的助手", text);
                    String segmentSummary = gptService.requestLlama3("llama3-70b-8192", "你是一个擅长将大段文字总结成简单摘要的程序员", "将以下内容总结成简单摘要\n" + text);
//                    System.out.println("segmentSummary" + segmentSummary);
                    redisService.addSegmentSummary(discussId, segmentSummary);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentSummaryCursor(discussId, segmentSummaryCursor);
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