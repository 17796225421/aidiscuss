package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedisService {
    private JedisPool jedisPool;

    public RedisService() {
        // 这里需要根据实际情况初始化jedisPool
        jedisPool = new JedisPool("localhost", 6379);
    }

    private Jedis findDiscussDatabase(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> configGetResult = jedis.configGet("databases");
            int dbCount = Integer.parseInt(configGetResult.get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return jedis;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAllDiscussId() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> allDiscussId = new ArrayList<>();
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId")) {
                    String discussId = jedis.get("discussId");
                    allDiscussId.add(discussId);
                }
            }
            return allDiscussId;
        }
    }

    public List<DiscussBaseInfo> getDiscussBaseInfoList() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<DiscussBaseInfo> discussBaseInfoList = new ArrayList<>();
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussName")) {
                    String discussName = jedis.get("discussName");
                    String discussId = jedis.get("discussId");
                    String discussStatus = jedis.get("discussStatus");
                    DiscussBaseInfo discussBaseInfo = new DiscussBaseInfo(discussId, discussName, Integer.parseInt(discussStatus));
                    discussBaseInfoList.add(discussBaseInfo);
                }
            }
            return discussBaseInfoList;
        }
    }

    public void createDiscuss(DiscussInfo discussInfo) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.dbSize() == 0) {
                    jedis.set("discussId", discussInfo.getDiscussId());
                    jedis.set("discussName", discussInfo.getDiscussName());
                    jedis.set("discussStatus", String.valueOf(discussInfo.getDiscussStatus()));

                    jedis.del("sentenceList");
                    jedis.del("startTimeList");
                    jedis.del("stopTimeList");
                    jedis.del("segmentSummaryList");
                    jedis.del("segmentQuestionList");
                    jedis.del("segmentUnderstandList");
                    jedis.del("segmentRemarkList");
                    jedis.del("segmentRestateList");
                    jedis.del("segmentAnalogyList");
                    jedis.del("segmentContinueList");
                    jedis.del("timeSlicedSummaryList");
                    jedis.del("keyWordList");
                    jedis.del("keySentenceList");
                    jedis.del("backgroundList");
                    jedis.del("questionAnswerList");

                    jedis.set("segmentSummaryCursor", "0");
                    jedis.set("segmentCorrectCursor", "0");
                    jedis.set("sentenceSummaryCursor", "0");
                    jedis.set("segmentQuestionCursor", "0");
                    jedis.set("segmentRemarkCursor", "0");
                    jedis.set("segmentRestateCursor", "0");
                    jedis.set("segmentAnalogyCursor", "0");
                    jedis.set("segmentContinueCursor", "0");
                    jedis.set("segmentUnderstandCursor", "0");
                    jedis.set("timeSlicedSummaryCursor", "0");
                    jedis.set("keyWordCursor", "0");
                    jedis.set("keySentenceCursor", "0");
                    break;
                }
            }
        }
    }

    public DiscussInfo getDiscussInfo(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        DiscussInfo discussInfo = new DiscussInfo();

        discussInfo.setDiscussId(jedis.get("discussId"));
        discussInfo.setDiscussName(jedis.get("discussName"));
        discussInfo.setDiscussStatus(Integer.parseInt(jedis.get("discussStatus")));

        List<String> sentenceJsonList = jedis.lrange("sentenceList", 0, -1);
        List<Sentence> sentenceList = sentenceJsonList.stream()
                .map(json -> new Gson().fromJson(json, Sentence.class))
                .collect(Collectors.toList());
        discussInfo.setSentenceList(sentenceList);
        discussInfo.setStartTimeList(jedis.lrange("startTimeList", 0, -1));
        discussInfo.setStopTimeList(jedis.lrange("stopTimeList", 0, -1));
        discussInfo.setSegmentSummaryList(jedis.lrange("segmentSummaryList", 0, -1));
        discussInfo.setSegmentQuestionList(jedis.lrange("segmentQuestionList", 0, -1));
        discussInfo.setSegmentUnderstandList(jedis.lrange("segmentUnderstandList", 0, -1));
        discussInfo.setSegmentRemarkList(jedis.lrange("segmentRemarkList", 0, -1));
        discussInfo.setSegmentRestateList(jedis.lrange("segmentRestateList", 0, -1));
        discussInfo.setSegmentAnalogyList(jedis.lrange("segmentAnalogyList", 0, -1));
        discussInfo.setSegmentContinueList(jedis.lrange("segmentAnalogyList", 0, -1));
        discussInfo.setTimeSlicedSummaryList(jedis.lrange("timeSlicedSummaryList", 0, -1));
        discussInfo.setKeyWordList(jedis.lrange("keyWordList", 0, -1));
        discussInfo.setKeySentenceList(jedis.lrange("keySentenceList", 0, -1));
        discussInfo.setBackgroundList(jedis.lrange("backgroundList", 0, -1));
        List<String> questionAnswerJsonList = jedis.lrange("questionAnswerList", 0, -1);
        List<QuestionAnswer> questionAnswerList = questionAnswerJsonList.stream()
                .map(json -> new Gson().fromJson(json, QuestionAnswer.class))
                .collect(Collectors.toList());
        discussInfo.setQuestionAnswerList(questionAnswerList);

        discussInfo.setSegmentSummaryCursor(Integer.parseInt(jedis.get("segmentSummaryCursor")));
        discussInfo.setSegmentCorrectCursor(Integer.parseInt(jedis.get("segmentCorrectCursor")));
        discussInfo.setSentenceSummaryCursor(Integer.parseInt(jedis.get("sentenceSummaryCursor")));
        discussInfo.setSegmentQuestionCursor(Integer.parseInt(jedis.get("segmentQuestionCursor")));
        discussInfo.setSegmentUnderstandCursor(Integer.parseInt(jedis.get("segmentUnderstandCursor")));
        discussInfo.setSegmentRemarkCursor(Integer.parseInt(jedis.get("segmentRemarkCursor")));
        discussInfo.setSegmentRestateCursor(Integer.parseInt(jedis.get("segmentRestateCursor")));
        discussInfo.setSegmentAnalogyCursor(Integer.parseInt(jedis.get("segmentAnalogyCursor")));
        discussInfo.setSegmentContinueCursor(Integer.parseInt(jedis.get("segmentAnalogyCursor")));
        discussInfo.setTimeSlicedSummaryCursor(Integer.parseInt(jedis.get("timeSlicedSummaryCursor")));
        discussInfo.setKeyWordCursor(Integer.parseInt(jedis.get("keyWordCursor")));
        discussInfo.setKeySentenceCursor(Integer.parseInt(jedis.get("keySentenceCursor")));

        return discussInfo;
    }

    public void clearDiscuss(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.flushDB();
    }

    public void addSentence(String discussId, Sentence sentence) {
        Jedis jedis = findDiscussDatabase(discussId);
        String sentenceJson = new Gson().toJson(sentence);
        jedis.rpush("sentenceList", sentenceJson);
    }

    public List<Sentence> getSentences(String discussId) {
        List<Sentence> sentenceList = new ArrayList<>();
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> sentenceJsonList = jedis.lrange("sentenceList", 0, -1);
        for (String sentenceJson : sentenceJsonList) {
            Sentence sentence = new Gson().fromJson(sentenceJson, Sentence.class);
            sentenceList.add(sentence);
        }
        return sentenceList;
    }

    public void addStartTime(String discussId, String startTime) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("startTimeList", startTime);
    }

    public void addStopTime(String discussId, String stopTime) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("stopTimeList", stopTime);
    }

    public void updateDiscussStatus(String discussId, DiscussStatusEnum discussStatusEnum) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("discussStatus", String.valueOf(discussStatusEnum.getValue()));
    }

    public int getSegmentSummaryCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentSummaryCursor"));
    }

    public int getTimeSlicedSummaryCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("timeSlicedSummaryCursor"));
    }

    public void setSegmentSummaryCursor(String discussId, int segmentSummaryCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentSummaryCursor", String.valueOf(segmentSummaryCursor));
    }

    public void setTimeSlicedSummaryCursor(String discussId, int timeSlicedSummaryCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("timeSlicedSummaryCursor", String.valueOf(timeSlicedSummaryCursor));
    }

    public void addSegmentSummary(String discussId, String segmentSummary) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentSummaryList", segmentSummary);
    }

    public void addTimeSlicedSummary(String discussId, String timeSlicedSummary) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("timeSlicedSummaryList", timeSlicedSummary);
    }

    public int getKeyWordCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        if (jedis != null) {
            String cursor = jedis.get("keyWordCursor");
            return Integer.parseInt(cursor);
        }
        return 0;
    }

    public int getKeySentenceCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        if (jedis != null) {
            String cursor = jedis.get("keySentenceCursor");
            return Integer.parseInt(cursor);
        }
        return 0;
    }

    public void setKeyWordCursor(String discussId, int keyWordCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("keyWordCursor", String.valueOf(keyWordCursor));
    }

    public void setKeySentenceCursor(String discussId, int keySentenceCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("keySentenceCursor", String.valueOf(keySentenceCursor));
    }

    public void addKeyWordList(String discussId, List<String> newKeyWordList) {
        Jedis jedis = findDiscussDatabase(discussId);
        for (String newKeyWord : newKeyWordList) {
            jedis.rpush("keyWordList", newKeyWord);
        }
    }

    public void addKeySentenceList(String discussId, List<String> newKeySentenceList) {
        Jedis jedis = findDiscussDatabase(discussId);
        for (String newKeySentence : newKeySentenceList) {
            jedis.rpush("keySentenceList", newKeySentence);
        }
    }

    public List<QuestionAnswer> getQuestionAnswerList(String discussId) {
        List<QuestionAnswer> questionAnswerList = new ArrayList<>();
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> questionAnswerJsonList = jedis.lrange("questionAnswerList", 0, -1);
        for (String questionAnswerJson : questionAnswerJsonList) {
            QuestionAnswer questionAnswer = new Gson().fromJson(questionAnswerJson, QuestionAnswer.class);
            questionAnswerList.add(questionAnswer);
        }
        return questionAnswerList;
    }

    public void addQuestionAnswer(String discussId, QuestionAnswer questionAnswer) {
        Jedis jedis = findDiscussDatabase(discussId);
        String questionAnswerJson = new Gson().toJson(questionAnswer);
        jedis.rpush("questionAnswerList", questionAnswerJson);
    }

    public List<String> getBackgroundList(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return jedis.lrange("backgroundList", 0, -1);
    }

    public void addBackground(String discussId, String background) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("backgroundList", background);
    }

    public void setBackground(String discussId, int index, String background) {
        Jedis jedis = findDiscussDatabase(discussId);
        long backgroundListLen = jedis.llen("backgroundList");
        if (index < 0 || index >= backgroundListLen) {
            return;
        }
        jedis.lset("backgroundList", index, background);
    }
    public void deleteBackground(String discussId, int index) {
        Jedis jedis = findDiscussDatabase(discussId);
        long backgroundListLen = jedis.llen("backgroundList");
        if (index < 0 || index >= backgroundListLen) {
            return;
        }
        String elementToRemove = jedis.lindex("backgroundList", index);
        jedis.lrem("backgroundList", 0, elementToRemove);
    }

    public int getSegmentQuestionCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentQuestionCursor"));
    }

    public void addSegmentQuestion(String discussId, String segmentQuestion) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentQuestionList", segmentQuestion);
    }

    public void setSegmentQuestionCursor(String discussId, int segmentQuestionCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentQuestionCursor", String.valueOf(segmentQuestionCursor));
    }

    public int getSegmentUnderstandCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentQuestionCursor"));
    }

    public void addSegmentUnderstand(String discussId, String segmentUnderstand) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentUnderstandList", segmentUnderstand);
    }

    public void setSegmentUnderstandCursor(String discussId, int segmentUnderstandCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentUnderstandCursor", String.valueOf(segmentUnderstandCursor));
    }

    public List<String> getStopTimeList(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return jedis.lrange("stopTimeList", 0, -1);
    }

    public List<String> getStartTimeList(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return jedis.lrange("startTimeList", 0, -1);
    }

    public int getSentenceSummaryCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("sentenceSummaryCursor"));
    }

    public void setSentenceSummary(String discussId, int sentenceSummaryCursor, String summary) {
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> sentenceJsonList = jedis.lrange("sentenceList", 0, -1);
        List<Sentence> sentenceList = sentenceJsonList.stream()
                .map(json -> new Gson().fromJson(json, Sentence.class))
                .collect(Collectors.toList());

        if (sentenceSummaryCursor >= 0 && sentenceSummaryCursor < sentenceList.size()) {
            Sentence sentence = sentenceList.get(sentenceSummaryCursor);
            sentence.setSummary(summary);
            String updatedJson = new Gson().toJson(sentence);
            jedis.lset("sentenceList", sentenceSummaryCursor, updatedJson);
        }
    }

    public void setSentenceSummaryCursor(String discussId, int sentenceSummaryCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("sentenceSummaryCursor", String.valueOf(sentenceSummaryCursor));
    }

    public int getSegmentCorrectCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        if (jedis != null) {
            String cursor = jedis.get("segmentCorrectCursor");
            return Integer.parseInt(cursor);
        }
        return 0;
    }

    public void updateSentenceText(String discussId, int index, String sentenceText) {
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> sentenceJsonList = jedis.lrange("sentenceList", 0, -1);
        List<Sentence> sentenceList = sentenceJsonList.stream()
                .map(json -> new Gson().fromJson(json, Sentence.class))
                .collect(Collectors.toList());

        if (index >= 0 && index < sentenceList.size()) {
            Sentence sentence = sentenceList.get(index);
            sentence.setText(sentenceText);
            String updatedJson = new Gson().toJson(sentence);
            jedis.lset("sentenceList", index, updatedJson);
        }
    }

    public void setSegmentCorrectCursor(String discussId, int segmentCorrectCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentCorrectCursor", String.valueOf(segmentCorrectCursor));
    }

    public int getSegmentRemarkCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentRemarkCursor"));
    }

    public void addSegmentRemark(String discussId, String segmentRemark) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentRemarkList", segmentRemark);
    }

    public void setSegmentRemarkCursor(String discussId, int segmentRemarkCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentRemarkCursor", String.valueOf(segmentRemarkCursor));
    }
    public int getSegmentRestateCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentRestateCursor"));
    }

    public void addSegmentRestate(String discussId, String segmentRestate) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentRestateList", segmentRestate);
    }

    public void setSegmentRestateCursor(String discussId, int segmentRestateCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentRestateCursor", String.valueOf(segmentRestateCursor));
    }
    public int getSegmentAnalogyCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentAnalogyCursor"));
    }

    public void addSegmentAnalogy(String discussId, String segmentAnalogy) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentAnalogyList", segmentAnalogy);
    }

    public void setSegmentAnalogyCursor(String discussId, int segmentAnalogyCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentAnalogyCursor", String.valueOf(segmentAnalogyCursor));
    }
    public int getSegmentContinueCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentContinueCursor"));
    }

    public void addSegmentContinue(String discussId, String segmentContinue) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentContinueList", segmentContinue);
    }

    public void setSegmentContinueCursor(String discussId, int segmentContinueCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentContinueCursor", String.valueOf(segmentContinueCursor));
    }
}