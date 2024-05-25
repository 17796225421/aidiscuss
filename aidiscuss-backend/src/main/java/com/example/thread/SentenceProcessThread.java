package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.example.util.GptResUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SentenceProcessThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();

    public SentenceProcessThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);
                int sentenceProcessCursor = redisService.getSentenceProcessCursor(discussId);
                List<String> backgroundList = redisService.getBackgroundList(discussId);
                if (sentenceProcessCursor < sentenceList.size()) {
                    Sentence sentenceProcess = sentenceList.get(sentenceProcessCursor);
                    JSONArray sentencesArray = new JSONArray();
                    for (int i = 0; i < sentenceProcessCursor; i++) {
                        Sentence sentence = sentenceList.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("text", sentence.getText());
                        jsonObject.put("score", sentence.getScore());
                        sentencesArray.put(jsonObject);
                    }



                    int retryCount = 0;
                    boolean success = false;
                    while (retryCount < 3 && !success) {
                        try {
                            String gptJson = gptService.requestQwen("qwen1.5-110b-chat",
                                    "\n用JSON格式返回，格式为{\"correct\":语音转录纠正, \"summary\": 纠正后的缩句, \"score\": 内容质量评分}",
                                    "背景信息：" + backgroundList.toString() + "\n背景信息结束\n" +
                                            "历史讨论语音转录记录：" + sentencesArray + "\n" + "对于以下这条转录记录，生成这条转录记录的语音转录纠正、纠正后的缩句、内容质量评分。内容质量评分范围1到5，评估与整体对话内容相比的重要性。\n" + sentenceProcess.getText());

                            JSONObject jsonObject = GptResUtils.StringToJson(gptJson);
                            sentenceProcess.setText(jsonObject.getString("correct"));
                            sentenceProcess.setSummary(jsonObject.getString("summary"));
                            sentenceProcess.setScore(jsonObject.getInt("score"));

                            redisService.setSentenceProcess(discussId, sentenceProcessCursor, sentenceProcess);
                            success = true;
                        } catch (Exception e) {
                            retryCount++;
                        }
                    }

                    if (!success) {
                        // 超过重试次数，只更新 sentenceProcessCursor
                        redisService.setSentenceProcessCursor(discussId, sentenceProcessCursor + 1);
                    } else {
                        redisService.setSentenceProcessCursor(discussId, sentenceProcessCursor + 1);
                    }
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