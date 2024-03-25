package com.example.thread;

import com.example.model.MicTypeEnum;
import com.example.model.Sentencetmp;
import com.example.model.Sentencestmp;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;

public class SentenceSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = RedisService.getInstance();
    private GptService gptService = new GptService();

    public SentenceSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // 从redis取出externMicSentences、wireMicSentences和virtualMicSentences
                String externMicSentencesJson = redisService.getExternMicSentences(discussId);
                String wireMicSentencesJson = redisService.getWireMicSentences(discussId);
                String virtualMicSentencesJson = redisService.getVirtualMicSentences(discussId);

                Gson gson = new Gson();
                Sentencestmp externMicSentencestmp = gson.fromJson(externMicSentencesJson, Sentencestmp.class);
                Sentencestmp wireMicSentencestmp = gson.fromJson(wireMicSentencesJson, Sentencestmp.class);
                Sentencestmp virtualMicSentencestmp = gson.fromJson(virtualMicSentencesJson, Sentencestmp.class);

                // 处理externMicSentences
                processSentences(externMicSentencestmp, MicTypeEnum.EXTERN);

                // 处理wireMicSentences
                processSentences(wireMicSentencestmp, MicTypeEnum.WIRE);

                // 处理virtualMicSentences
                processSentences(virtualMicSentencestmp, MicTypeEnum.VIRTUAL);

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSentences(Sentencestmp sentencestmp, MicTypeEnum micType) throws Exception {
        for (Sentencetmp sentencetmp : sentencestmp.getQueue()) {
            if (sentencetmp.getSummary().isEmpty()) {
                // 调用GPT-3生成摘要
                String summary = gptService.requestGpt3("gpt-3.5-turbo", "请为以下句子生成一个简短的中文摘要:", sentencetmp.getText());
                System.out.println(111 + summary);
                sentencetmp.setSummary(summary);
            }
        }
        // 更新到redis中
        redisService.setMicSentences(discussId, micType, sentencestmp);
    }

    public void stopRunning() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}