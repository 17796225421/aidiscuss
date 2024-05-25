package com.example.service;

import com.example.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DiscussService {

    @Autowired
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();

    public DiscussInfo getDiscuss(String discussId) {
        return redisService.getDiscussInfo(discussId);
    }

    public DiscussInfo getDiscussInfo(String discussId) {
        return redisService.getDiscussInfo(discussId);
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

        processQuestionStream(inputBuilder.toString(), questionRequest);
    }

    private void processQuestionStream(String user, QuestionRequest questionRequest) {
        StringBuilder accumulativeContent = new StringBuilder();
        String lastContent = "";
        long lastPrintTime = 0;
        try {
            BufferedSource source = gptService.requestGpt4Stream("qwen1.5-110b-chat", "你是个有帮助的助手", user);

            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line != null && !line.isEmpty()) {
                    if (line.startsWith("data:")) {
                        String jsonStr = line.substring("data:".length()).trim();
                        if (jsonStr.equals("[DONE]")) {
                            break;
                        }
                        JsonObject data = JsonParser.parseString(jsonStr).getAsJsonObject();
                        JsonArray choices = data.getAsJsonArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").getAsString();
                                if (content != null) {
                                    accumulativeContent.append(content);
                                    long currentTime = System.currentTimeMillis();
                                    if (!accumulativeContent.toString().equals(lastContent) && currentTime - lastPrintTime >= 1000) {
                                        QuestionAnswer questionAnswer = new QuestionAnswer(questionRequest.getQuestion(), accumulativeContent.toString());
                                        redisService.setQuestionAnswer(questionRequest.getDiscussId(), questionAnswer);
                                        lastContent = accumulativeContent.toString();

                                        System.out.println(lastContent);
                                        lastPrintTime = currentTime;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            QuestionAnswer questionAnswer = new QuestionAnswer(questionRequest.getQuestion(), accumulativeContent.toString());
            redisService.setQuestionAnswer(questionRequest.getDiscussId(), questionAnswer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteQuestion(QuestionRequest questionRequest) {
        redisService.deleteQuestion(questionRequest.getDiscussId(), questionRequest.getQuestion());
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

    public Resource audio(String discussId, MicTypeEnum micTypeEnum) throws IOException {
        String discussName = redisService.getDiscussName(discussId);
        String baseDir = "data/";
        String formattedDiscussName = discussName.replace(" ", "_").replace(".", "-").replace(":", "-");
        String dirName = baseDir + formattedDiscussName;

        String fileName = dirName + "/" + micTypeEnum + ".wav";
        Path path = Paths.get(fileName);
        return new UrlResource(path.toUri());
    }

    public void postNoteText(String discussId, String text) {
        redisService.setNoteText(discussId, text);
    }

    public String uploadImage(MultipartFile file, String discussId) throws IOException {
        String discussName = redisService.getDiscussName(discussId);
        String baseDir = System.getProperty("user.dir") + "/data/";
        String formattedDiscussName = discussName.replace(" ", "_").replace(".", "-").replace(":", "-");
        String dirName = baseDir + formattedDiscussName;


        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String randomFileName = UUID.randomUUID().toString() + fileExtension;
        String filePath = dirName + "/" + randomFileName;
        file.transferTo(new File(filePath));

        String imageUrl = "http://127.0.0.1:10002/images/" + randomFileName;
        return imageUrl;
    }

    public Resource getImage(String fileName, String discussId) throws IOException {
        String discussName = redisService.getDiscussName(discussId);
        String baseDir = "data/";
        String formattedDiscussName = discussName.replace(" ", "_").replace(".", "-").replace(":", "-");
        String dirName = baseDir + formattedDiscussName;

        String filePath = dirName + "/" + fileName;
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        return resource;
    }
}