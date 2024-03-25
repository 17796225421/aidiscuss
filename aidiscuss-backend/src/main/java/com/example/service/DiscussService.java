package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DiscussService {

    @Autowired
    private RedisService redisService;
    private GptService gptService = new GptService();

    public DiscussInfo getDiscuss(String discussId) {
        // 调用RedisService的getDiscussInfo方法获取DiscussInfo
        return redisService.getDiscussInfo(discussId);
    }

    public String getExternMicSentences(String discussId) {
        // 直接调用RedisService的方法获取数据
        return RedisService.getInstance().getExternMicSentences(discussId);
    }

    public String getWireMicSentences(String discussId) {
        // 直接调用RedisService的方法获取数据
        return RedisService.getInstance().getWireMicSentences(discussId);
    }

    public String getVirtualMicSentences(String discussId) {
        // 直接调用RedisService的方法获取数据
        return RedisService.getInstance().getVirtualMicSentences(discussId);
    }

    public void askQuestion(QuestionRequest questionRequest) throws IOException {
        String discussId = questionRequest.getDiscussId();
        // 从RedisService中读取历史的问答信息
        List<QuestionAnswer> historyList = redisService.getQuestionAnswerList(questionRequest.getDiscussId());

        // 从Redis中获取三种类型的MicSentences数据
        String externMicSentencesJson = redisService.getExternMicSentences(discussId);
        String wireMicSentencesJson = redisService.getWireMicSentences(discussId);
        String virtualMicSentencesJson = redisService.getVirtualMicSentences(discussId);

        // 解析JSON字符串中的queue数组
        List<Sentence> externMicSentences = parseSentencesFromQueue(externMicSentencesJson);
        List<Sentence> wireMicSentences = parseSentencesFromQueue(wireMicSentencesJson);
        List<Sentence> virtualMicSentences = parseSentencesFromQueue(virtualMicSentencesJson);

        List<Sentence>allMicSentences=new ArrayList<>();
        allMicSentences.addAll(externMicSentences);
        allMicSentences.addAll(wireMicSentences);
        allMicSentences.addAll(virtualMicSentences);
        // 将所有句子按照开始时间排序
        Collections.sort(allMicSentences, Comparator.comparing(Sentence::getBeginTime));
        // 使用StringBuilder拼接所有句子的文本，并用换行符隔开
        StringBuilder allMicSentencesTextBuilder = new StringBuilder();
        for (Sentence sentence : allMicSentences) {
            allMicSentencesTextBuilder.append(sentence.getText()).append("\n");
        }
        // 去掉最后一个多余的换行符
        if (allMicSentencesTextBuilder.length() > 0) {
            allMicSentencesTextBuilder.setLength(allMicSentencesTextBuilder.length() - 1);
        }
        // 获取拼接后的文本字符串
        String allMicSentencesText = allMicSentencesTextBuilder.toString();

        // 构建GPT请求的输入
        StringBuilder inputBuilder = new StringBuilder();
        for (QuestionAnswer qa : historyList) {
            inputBuilder.append("Question: ").append(qa.getQuestion()).append("\n");
            inputBuilder.append("Answer: ").append(qa.getAnswer()).append("\n\n");
        }
        inputBuilder.append("Question: ").append(questionRequest.getQuestion()).append("\n");
        inputBuilder.append("Answer: ");
        inputBuilder.append("discussText: ").append(allMicSentencesText);
        System.out.println(inputBuilder.toString());
        String gptText = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", inputBuilder.toString());

        QuestionAnswer questionAnswer = new QuestionAnswer(questionRequest.getQuestion(), gptText);

        // 将更新后的历史信息保存到RedisService中
        redisService.addQuestionAnswer(questionRequest.getDiscussId(),questionAnswer);
    }

    /**
     * 解析JSON字符串中的queue数组，并将其转换为Sentence列表
     *
     * @param json JSON字符串
     * @return Sentence列表
     */
    private List<Sentence> parseSentencesFromQueue(String json) {
        // 使用Gson库解析JSON字符串
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        // 从JsonObject中获取名为queue的JsonArray
        String queueJson = jsonObject.getAsJsonArray("queue").toString();
        // 将JsonArray字符串转换为Sentence列表
        return gson.fromJson(queueJson, new TypeToken<List<Sentence>>() {
        }.getType());
    }

    public void updateBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        int index = backgroundRequest.getIndex();
        String background = backgroundRequest.getBackground();

        // 从Redis中获取背景列表
        List<String> backgroundList = redisService.getBackground(discussId);

        // 检查索引是否在有效范围内
        if (index >= 0 && index < backgroundList.size()) {
            // 更新指定索引位置的背景
            backgroundList.set(index, background);
            // 将更新后的背景列表保存到Redis中
            redisService.setBackground(discussId, backgroundList);
        } else {
            throw new IndexOutOfBoundsException("背景索引超出范围");
        }
    }

    public void addBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        String background = backgroundRequest.getBackground();

        // 从Redis中获取背景列表
        List<String> backgroundList = redisService.getBackground(discussId);

        // 将新的背景添加到列表末尾
        backgroundList.add(background);

        // 将更新后的背景列表保存到Redis中
        redisService.setBackground(discussId, backgroundList);
    }

    public void deleteBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        int index = backgroundRequest.getIndex();

        // 从Redis中获取背景列表
        List<String> backgroundList = redisService.getBackground(discussId);

        // 检查索引是否在有效范围内
        if (index >= 0 && index < backgroundList.size()) {
            // 删除指定索引位置的背景
            backgroundList.remove(index);
            // 将更新后的背景列表保存到Redis中
            redisService.setBackground(discussId, backgroundList);
        } else {
            throw new IndexOutOfBoundsException("背景索引超出范围");
        }
    }

    public List<String> getBackground(String discussId) {
        // 直接从Redis中获取背景列表并返回
        return redisService.getBackground(discussId);
    }
}