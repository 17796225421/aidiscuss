package com.example.thread;


import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class TimeSlicedSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true; // 添加 running 标志变量
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();

    public TimeSlicedSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // 周期一分钟
                Thread.sleep(60 * 1000);
                if (!running) {
                    break;
                }

                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int timeSlicedSummaryCursor = redisService.getTimeSlicedSummaryCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                if (timeSlicedSummaryCursor < sentenceList.size()) {
                    unprocessedText.append(sentenceList.get(timeSlicedSummaryCursor).getText());
                    timeSlicedSummaryCursor++;
                }

                if (unprocessedText.length() > 0) {
                    String text = unprocessedText.toString();
                    String timeSlicedSummary = gptService.requestLlama3("llama3-70b-8192", "你是一个有帮助的助手", text);
                    System.out.println("timeSlicedSummary" + timeSlicedSummary);
                    redisService.addTimeSlicedSummary(discussId, timeSlicedSummary);

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

}
