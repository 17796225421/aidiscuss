package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
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
                    jedis.set("realTimeSentence", "");
                    jedis.set("segmentDirectory", "");
                    jedis.set("noteText", "");

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
                    jedis.del("segmentMultiangleList");
                    jedis.del("segmentUmlList");
                    jedis.del("segmentLogicList");
                    jedis.del("segmentTeachList");
                    jedis.del("segmentManagingupList");
                    jedis.del("timeSlicedSummaryList");
                    jedis.del("keyWordList");
                    jedis.del("backgroundList");
                    jedis.del("questionAnswerList");

                    jedis.set("segmentSummaryCursor", "0");
                    jedis.set("sentenceSummaryCursor", "0");
                    jedis.set("segmentQuestionCursor", "0");
                    jedis.set("segmentRemarkCursor", "0");
                    jedis.set("segmentRestateCursor", "0");
                    jedis.set("segmentAnalogyCursor", "0");
                    jedis.set("segmentContinueCursor", "0");
                    jedis.set("segmentMultiangleCursor", "0");
                    jedis.set("segmentUmlCursor", "0");
                    jedis.set("segmentLogicCursor", "0");
                    jedis.set("segmentTeachCursor", "0");
                    jedis.set("segmentManagingupCursor", "0");
                    jedis.set("segmentUnderstandCursor", "0");
                    jedis.set("timeSlicedSummaryCursor", "0");
                    jedis.set("keyWordCursor", "0");
                    jedis.set("sentenceProcessCursor", "0");
                    jedis.set("segmentDirectoryCursor", "0");
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
        discussInfo.setRealTimeSentence(jedis.get("realTimeSentence"));
        discussInfo.setNoteText(jedis.get("noteText"));
        discussInfo.setSegmentDirectory(jedis.get("segmentDirectory"));

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
        discussInfo.setSegmentContinueList(jedis.lrange("segmentContinueList", 0, -1));
        discussInfo.setSegmentMultiangleList(jedis.lrange("segmentMultiangleList", 0, -1));
        discussInfo.setSegmentLogicList(jedis.lrange("segmentLogicList", 0, -1));
        discussInfo.setSegmentTeachList(jedis.lrange("segmentTeachList", 0, -1));
        discussInfo.setSegmentManagingupList(jedis.lrange("segmentManagingupList", 0, -1));
        discussInfo.setSegmentUmlList(jedis.lrange("segmentUmlList", 0, -1));
        discussInfo.setTimeSlicedSummaryList(jedis.lrange("timeSlicedSummaryList", 0, -1));
        discussInfo.setKeyWordList(jedis.lrange("keyWordList", 0, -1));
        discussInfo.setBackgroundList(jedis.lrange("backgroundList", 0, -1));
        List<String> questionAnswerJsonList = jedis.lrange("questionAnswerList", 0, -1);
        List<QuestionAnswer> questionAnswerList = questionAnswerJsonList.stream()
                .map(json -> new Gson().fromJson(json, QuestionAnswer.class))
                .collect(Collectors.toList());
        discussInfo.setQuestionAnswerList(questionAnswerList);

        discussInfo.setSegmentSummaryCursor(Integer.parseInt(jedis.get("segmentSummaryCursor")));
        discussInfo.setSegmentQuestionCursor(Integer.parseInt(jedis.get("segmentQuestionCursor")));
        discussInfo.setSegmentUnderstandCursor(Integer.parseInt(jedis.get("segmentUnderstandCursor")));
        discussInfo.setSegmentRemarkCursor(Integer.parseInt(jedis.get("segmentRemarkCursor")));
        discussInfo.setSegmentRestateCursor(Integer.parseInt(jedis.get("segmentRestateCursor")));
        discussInfo.setSegmentAnalogyCursor(Integer.parseInt(jedis.get("segmentAnalogyCursor")));
        discussInfo.setSegmentContinueCursor(Integer.parseInt(jedis.get("segmentContinueCursor")));
        discussInfo.setSegmentMultiangleCursor(Integer.parseInt(jedis.get("segmentMultiangleCursor")));
        discussInfo.setSegmentLogicCursor(Integer.parseInt(jedis.get("segmentLogicCursor")));
        discussInfo.setSegmentTeachCursor(Integer.parseInt(jedis.get("segmentTeachCursor")));
        discussInfo.setSegmentManagingupCursor(Integer.parseInt(jedis.get("segmentManagingupCursor")));
        discussInfo.setSegmentUmlCursor(Integer.parseInt(jedis.get("segmentUmlCursor")));
        discussInfo.setTimeSlicedSummaryCursor(Integer.parseInt(jedis.get("timeSlicedSummaryCursor")));
        discussInfo.setKeyWordCursor(Integer.parseInt(jedis.get("keyWordCursor")));
        discussInfo.setSentenceProcessCursor(Integer.parseInt(jedis.get("sentenceProcessCursor")));
        discussInfo.setSegmentDirectoryCursor(Integer.parseInt(jedis.get("segmentDirectoryCursor")));

        return discussInfo;
    }

    public String getDiscussName(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return jedis.get("discussName");
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

    public void setKeyWordCursor(String discussId, int keyWordCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("keyWordCursor", String.valueOf(keyWordCursor));
    }

    public void addKeyWordList(String discussId, List<String> newKeyWordList) {
        Jedis jedis = findDiscussDatabase(discussId);
        for (String newKeyWord : newKeyWordList) {
            jedis.rpush("keyWordList", newKeyWord);
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

    public void setQuestionAnswer(String discussId, QuestionAnswer questionAnswer) {
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> questionAnswerJsonList = jedis.lrange("questionAnswerList", 0, -1);
        List<QuestionAnswer> questionAnswerList = questionAnswerJsonList.stream()
                .map(json -> new Gson().fromJson(json, QuestionAnswer.class))
                .collect(Collectors.toList());

        int lastIndex = -1;
        for (int i = questionAnswerList.size() - 1; i >= 0; i--) {
            if (questionAnswerList.get(i).getQuestion().equals(questionAnswer.getQuestion())) {
                lastIndex = i;
                break;
            }
        }

        if (lastIndex != -1) {
            questionAnswerList.set(lastIndex, questionAnswer);
            String updatedJson = new Gson().toJson(questionAnswer);
            jedis.lset("questionAnswerList", lastIndex, updatedJson);
        } else {
            String questionAnswerJson = new Gson().toJson(questionAnswer);
            jedis.rpush("questionAnswerList", questionAnswerJson);
        }
    }

    public void deleteQuestion(String discussId, String question) {
        Jedis jedis = findDiscussDatabase(discussId);
        List<String> questionAnswerJsonList = jedis.lrange("questionAnswerList", 0, -1);
        List<QuestionAnswer> questionAnswerList = questionAnswerJsonList.stream()
                .map(json -> new Gson().fromJson(json, QuestionAnswer.class))
                .collect(Collectors.toList());

        int lastIndex = -1;
        for (int i = questionAnswerList.size() - 1; i >= 0; i--) {
            if (questionAnswerList.get(i).getQuestion().equals(question)) {
                lastIndex = i;
                break;
            }
        }

        if (lastIndex != -1) {
            jedis.lset("questionAnswerList", lastIndex, "");
            jedis.lrem("questionAnswerList", 0, "");
        }
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

    public int getSegmentMultiangleCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentMultiangleCursor"));
    }

    public void addSegmentMultiangle(String discussId, String segmentMultiangle) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentMultiangleList", segmentMultiangle);
    }

    public void setSegmentMultiangleCursor(String discussId, int segmentMultiangleCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentMultiangleCursor", String.valueOf(segmentMultiangleCursor));
    }

    public int getSegmentLogicCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentLogicCursor"));
    }

    public void addSegmentLogic(String discussId, String segmentLogic) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentLogicList", segmentLogic);
    }

    public void setSegmentLogicCursor(String discussId, int segmentLogicCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentLogicCursor", String.valueOf(segmentLogicCursor));
    }
    public int getSegmentTeachCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentTeachCursor"));
    }

    public void addSegmentTeach(String discussId, String segmentTeach) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentTeachList", segmentTeach);
    }

    public void setSegmentTeachCursor(String discussId, int segmentTeachCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentTeachCursor", String.valueOf(segmentTeachCursor));
    }

    public int getSegmentManagingupCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentManagingupCursor"));
    }

    public void addSegmentManagingup(String discussId, String segmentManagingup) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentManagingupList", segmentManagingup);
    }

    public void setSegmentManagingupCursor(String discussId, int segmentManagingupCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentManagingupCursor", String.valueOf(segmentManagingupCursor));
    }

    public void updateRealTimeSentence(String discussId, String realTimeSentence) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("realTimeSentence", realTimeSentence);
    }

    public int getSentenceProcessCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("sentenceProcessCursor"));
    }

    public void setSentenceProcess(String discussId, int sentenceProcessCursor, Sentence sentenceProcess) {
        Jedis jedis = findDiscussDatabase(discussId);
        String sentenceJson = new Gson().toJson(sentenceProcess);
        jedis.lset("sentenceList", sentenceProcessCursor, sentenceJson);
    }

    public void setSentenceProcessCursor(String discussId, int sentenceProcessCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("sentenceProcessCursor", String.valueOf(sentenceProcessCursor));
    }

    // 获取当前讨论的segmentDirectoryCursor
    public int getSegmentDirectoryCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return Integer.parseInt(jedis.get("segmentDirectoryCursor"));
    }

    // 获取当前讨论的segmentDirectory
    public String getSegmentDirectory(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        return jedis.get("segmentDirectory");
    }

    // 更新当前讨论的segmentDirectory
    public void updateSegmentDirectory(String discussId, String newSegmentDirectory) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentDirectory", newSegmentDirectory);
    }

    // 设置当前讨论的segmentDirectoryCursor
    public void setSegmentDirectoryCursor(String discussId, int newCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentDirectoryCursor", String.valueOf(newCursor));
    }

    public int getSegmentUmlCursor(String discussId) {
        Jedis jedis = findDiscussDatabase(discussId);
        String cursor = jedis.get("segmentUmlCursor");
        return cursor != null ? Integer.parseInt(cursor) : 0;
    }

    public void addSegmentUml(String discussId, String segmentUml) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.rpush("segmentUmlList", segmentUml);
    }

    public void setSegmentUmlCursor(String discussId, int segmentUmlCursor) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("segmentUmlCursor", String.valueOf(segmentUmlCursor));
    }

    public void setNoteText(String discussId, String text) {
        Jedis jedis = findDiscussDatabase(discussId);
        jedis.set("noteText", text);
    }
}