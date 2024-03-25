package com.example.thread;

import com.example.model.MicTypeEnum;
import com.example.model.Sentence;
import com.example.model.Sentences;
import com.example.service.GptService;
import com.example.service.RedisService;
import com.google.gson.Gson;

import static com.example.model.MicTypeEnum.*;

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
                Sentences externMicSentences = gson.fromJson(externMicSentencesJson, Sentences.class);
                Sentences wireMicSentences = gson.fromJson(wireMicSentencesJson, Sentences.class);
                Sentences virtualMicSentences = gson.fromJson(virtualMicSentencesJson, Sentences.class);

                // 处理externMicSentences
                processSentences(externMicSentences, MicTypeEnum.EXTERN);

                // 处理wireMicSentences
                processSentences(wireMicSentences, MicTypeEnum.WIRE);

                // 处理virtualMicSentences
                processSentences(virtualMicSentences, MicTypeEnum.VIRTUAL);

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSentences(Sentences sentences, MicTypeEnum micType) throws Exception {
        for (Sentence sentence : sentences.getQueue()) {
            if (sentence.getSummary().isEmpty()) {
                // 调用GPT-3生成摘要
                String summary = gptService.requestGpt3("gpt-3.5-turbo", "请为以下句子生成一个简短的中文摘要:", sentence.getText());
                System.out.println(111 + summary);
                sentence.setSummary(summary);
            }
        }
        // 更新到redis中
        redisService.setMicSentences(discussId, micType, sentences);
    }

    public void stopRunning() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}