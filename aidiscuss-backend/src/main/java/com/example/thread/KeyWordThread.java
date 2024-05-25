package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

public class KeyWordThread extends Thread {
    private String discussId;
    private volatile boolean running = true; // 添加 running 标志变量
    private RedisService redisService =new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;
    public KeyWordThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int keyWordCursor = redisService.getKeyWordCursor(discussId);
                List<String> backgroundList = redisService.getBackgroundList(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (keyWordCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(keyWordCursor).getText());
                    keyWordCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String gptText = gptService.requestQwen("qwen1.5-110b-chat", "你擅长从大段文字中找出关键词组",
                            "背景信息：" + backgroundList.toString() + "\n背景信息结束\n" +
                                    "给你一段文字，找出关键词组，每个关键词语输出一行：" + text);
//                    String gptText = gptService.requestQwen("qwen1.5-110b-chat", "你是一个有帮助的助手", text);
                    List<String> keyWordList = Arrays.asList(gptText.split("\n"));
                    redisService.addKeyWordList(discussId, keyWordList);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setKeyWordCursor(discussId, keyWordCursor);
                }

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        running = false; // 设置 running 为 false,通知麦克风线程停止运行
    }

    public boolean isRunning() {
        return running;
    }
}
