package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;

import java.util.List;

public class SentenceSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();

    public SentenceSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);
                int sentenceSummaryCursor = redisService.getSentenceSummaryCursor(discussId);
                if (sentenceSummaryCursor < sentenceList.size()) {
                    String text = sentenceList.get(sentenceSummaryCursor).getText();
                    String gptText = gptService.requestGpt3("gpt-3.5-turbo-0125", "缩句，越短越好", text);
                    System.out.println("gptText "+gptText);
                    redisService.setSentenceSummary(discussId, sentenceSummaryCursor, gptText);
                    redisService.setSentenceSummaryCursor(discussId, sentenceSummaryCursor + 1);
                }
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