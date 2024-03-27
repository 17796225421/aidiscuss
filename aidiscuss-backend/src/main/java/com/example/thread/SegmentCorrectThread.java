package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;

import java.util.Arrays;
import java.util.List;

public class SegmentCorrectThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 10;

    public SegmentCorrectThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentCorrectCursor = redisService.getSegmentCorrectCursor(discussId);
                int begin = segmentCorrectCursor;
                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentCorrectCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentCorrectCursor).getText());
                    segmentCorrectCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String gptText = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", text);
                    gptText="1\n2\n3\n4\n5\n6\n7\n8\n9";
                    List<String> sentencesText = Arrays.asList(gptText.split("\n"));
                    for (int i = begin; i < segmentCorrectCursor; i++) {
                        redisService.updateSentenceText(discussId, i, sentencesText.get(i));
                    }
                    redisService.setSegmentCorrectCursor(discussId, segmentCorrectCursor);
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