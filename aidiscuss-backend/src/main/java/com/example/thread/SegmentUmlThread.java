package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.example.util.GptResUtils;

import java.util.List;

public class SegmentUmlThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentUmlThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentUmlCursor = redisService.getSegmentUmlCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentUmlCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentUmlCursor).getText());
                    segmentUmlCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentUml = gptService.requestGpt4("gpt-4-turbo-2024-04-09",
                            "你擅长生成plantuml代码",
                            "根据以下内容，生成plantuml代码逻辑：" + text);

                    String plantUml = GptResUtils.getPlantUml(segmentUml);

                    redisService.addSegmentUml(discussId, plantUml);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentUmlCursor(discussId, segmentUmlCursor);
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