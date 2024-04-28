package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;
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
                if (sentenceProcessCursor < sentenceList.size()) {
                    Sentence sentenceProcess = sentenceList.get(sentenceProcessCursor);
                    JSONArray background = new JSONArray();
                    for (int i = 0; i < sentenceProcessCursor; i++) {
                        Sentence sentence = sentenceList.get(i);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("text", sentence.getText());
                        jsonObject.put("score", sentence.getScore());
                        background.put(jsonObject);
                    }
                    String gptJson = gptService.requestGpt4Json("gpt-4-turbo-2024-04-09",
                            "\n用JSON格式返回，格式为{\"correct\":语音转录纠正, \"summary\": 纠正后的缩句, \"score\": 内容质量评分}",
                            background + "以上是讨论的语音转录记录。\n对于以下这条转录记录，生成这条转录记录的语音转录纠正、纠正后的缩句、内容质量评分。内容质量评分范围0到10，评估与整体对话内容相比的重要性。\n" + sentenceProcess.getText());

                    // 去除开头的非JSON字符
                    int startIndex = gptJson.indexOf("{");
                    if (startIndex != -1) {
                        gptJson = gptJson.substring(startIndex);
                    }

                    // 去除结尾的非JSON字符
                    int endIndex = gptJson.lastIndexOf("}");
                    if (endIndex != -1) {
                        gptJson = gptJson.substring(0, endIndex + 1);
                    }

                    JSONObject jsonObject = new JSONObject(gptJson);
                    sentenceProcess.setText(jsonObject.getString("correct"));
                    sentenceProcess.setSummary(jsonObject.getString("summary"));
                    sentenceProcess.setScore(jsonObject.getInt("score"));

                    redisService.setSentenceProcess(discussId, sentenceProcessCursor, sentenceProcess);
                    redisService.setSentenceProcessCursor(discussId, sentenceProcessCursor + 1);
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