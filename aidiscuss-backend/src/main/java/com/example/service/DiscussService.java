package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DiscussService {

    @Autowired
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();

    public DiscussInfo getDiscuss(String discussId) {
        return redisService.getDiscussInfo(discussId);
    }

    public List<Sentence> getSentences(String discussId) {
        return redisService.getSentences(discussId);
    }

    public void askQuestion(QuestionRequest questionRequest) throws IOException {
        String discussId = questionRequest.getDiscussId();
        List<QuestionAnswer> questionAnswerList = redisService.getQuestionAnswerList(questionRequest.getDiscussId());
        List<Sentence> sentenceList = redisService.getSentences(discussId);

        Collections.sort(sentenceList, Comparator.comparing(Sentence::getBeginTime));
        // 使用StringBuilder拼接所有句子的文本，并用换行符隔开
        StringBuilder allMicSentencesTextBuilder = new StringBuilder();
        for (Sentence sentence : sentenceList) {
            allMicSentencesTextBuilder.append(sentence.getText()).append("\n");
        }
        // 获取拼接后的文本字符串
        String allMicSentencesText = allMicSentencesTextBuilder.toString();

        // 构建GPT请求的输入
        StringBuilder inputBuilder = new StringBuilder();
        for (QuestionAnswer questionAnswer : questionAnswerList) {
            inputBuilder.append("Question: ").append(questionAnswer.getQuestion()).append("\n");
            inputBuilder.append("Answer: ").append(questionAnswer.getAnswer()).append("\n\n");
        }
        inputBuilder.append("Question: ").append(questionRequest.getQuestion()).append("\n");
        inputBuilder.append("Answer: ");
        inputBuilder.append("discussText: ").append(allMicSentencesText);
        System.out.println(inputBuilder.toString());
        String gptText = gptService.requestGpt3("gpt-3.5-turbo-0125", "你是一个有帮助的助手", inputBuilder.toString());

        QuestionAnswer questionAnswer = new QuestionAnswer(questionRequest.getQuestion(), gptText);

        // 将更新后的历史信息保存到RedisService中
        redisService.addQuestionAnswer(questionRequest.getDiscussId(), questionAnswer);
    }

    public void updateBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        int index = backgroundRequest.getIndex();
        String background = backgroundRequest.getBackground();
        redisService.setBackground(discussId, index, background);
    }

    public void addBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        String background = backgroundRequest.getBackground();
        redisService.addBackground(discussId, background);
    }

    public void deleteBackground(BackgroundRequest backgroundRequest) {
        String discussId = backgroundRequest.getDiscussId();
        int index = backgroundRequest.getIndex();
        redisService.deleteBackground(discussId, index);
    }

    public List<String> getBackground(String discussId) {
        // 直接从Redis中获取背景列表并返回
        return redisService.getBackgroundList(discussId);
    }
}