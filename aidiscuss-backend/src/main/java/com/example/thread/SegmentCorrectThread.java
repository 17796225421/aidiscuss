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
    private static final int MAX_TEXT_LENGTH = 100;

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
                    String gptText = gptService.requestGpt4("gpt-4-turbo-2024-04-09", "你擅长对语音识别生成的文字进行纠正", "给你一段语音识别生成的文字，输出纠正后的结果：" + text);
//                    String gptText = gptService.requestLlama3("llama3-70b-8192", "你是一个文字纠正大师", "给你一段语音识别文字，尽可能纠正识别结果，只输出纠正后的内容：" + text);
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