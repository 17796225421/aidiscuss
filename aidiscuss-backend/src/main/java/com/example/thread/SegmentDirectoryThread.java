package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.example.util.GptResUtils;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentDirectoryThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentDirectoryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);
                int segmentDirectoryCursor = redisService.getSegmentDirectoryCursor(discussId);
                List<String> backgroundList = redisService.getBackgroundList(discussId);
                String segmentDirectory = redisService.getSegmentDirectory(discussId);

                List<Map<String, Object>> unprocessedTextList = new ArrayList<>();
                int currentTextLength = 0;

                while (segmentDirectoryCursor < sentenceList.size() && currentTextLength <= MAX_TEXT_LENGTH) {
                    Sentence sentence = sentenceList.get(segmentDirectoryCursor);
                    Map<String, Object> sentenceData = new HashMap<>();
                    sentenceData.put("index", segmentDirectoryCursor);
                    sentenceData.put("text", sentence.getText());
                    unprocessedTextList.add(sentenceData);
                    currentTextLength += sentence.getText().length();
                    segmentDirectoryCursor++;
                }

                if (currentTextLength > MAX_TEXT_LENGTH) {
                    Gson gson = new Gson();
                    String text = gson.toJson(unprocessedTextList);

                    int retryCount = 0;
                    boolean success = false;
                    while (retryCount < 3 && !success) {
                        try {
                            String gptJson = gptService.requestQwen("qwen1.5-110b-chat",
                                    "用JSON格式返回,格式为{ \"data\": [ { \"dir\": \"一级目录名\", \"i\": 起始句子索引（整数）, \"sub\": [ { \"dir\": \"二级目录名\", \"i\": 起始句子索引, \"sub\": [ { \"dir\": \"三级目录名\", \"i\": 起始句子索引 } ] } ] } ] }",
                                    "背景信息：" + backgroundList.toString() + "\n背景信息结束\n" +
                                            "给你会议讨论的内容片段,是多条句子索引和句子。思考这些内容能不能给原目录补充一些目录项。\n目录应该越精简越概括越好,本次对原目录的修改应该尽可能少,第三级目录不存在sub\n" +
                                            "原目录:" + segmentDirectory + "\n会议讨论内容片段:" + text);
                            JSONObject jsonObject = GptResUtils.StringToJson(gptJson);
                            redisService.updateSegmentDirectory(discussId, jsonObject.toString());
                            success = true;
                        } catch (Exception e) {
                            retryCount++;
                        }
                    }

                    redisService.setSegmentDirectoryCursor(discussId, segmentDirectoryCursor);
                }
                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
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