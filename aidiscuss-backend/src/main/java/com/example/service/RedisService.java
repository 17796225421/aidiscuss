package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {
    // 使用volatile保证多线程环境下的可见性
    private static volatile RedisService instance;
    private JedisPool jedisPool;

    private RedisService() {
        // 这里需要根据实际情况初始化jedisPool
        jedisPool = new JedisPool("localhost", 6379);
    }

    // 使用双重检查锁优化getInstance()方法
    public static RedisService getInstance() {
        if (instance == null) {
            synchronized (RedisService.class) {
                if (instance == null) {
                    instance = new RedisService();
                }
            }
        }
        return instance;
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
                    String sentences = new Gson().toJson(new Sentences());
                    jedis.set("externMicSentences", sentences);
                    jedis.set("wireMicSentences", sentences);
                    jedis.set("virtualMicSentences", sentences);
                    jedis.set("startTimeList", "");
                    jedis.set("stopTimeList", "");
                    jedis.set("discussStatus", String.valueOf(DiscussStatusEnum.CREATED.getValue()));
                    String cursor = new Gson().toJson(new Cursor());
                    jedis.set("segmentSummaryCursor", cursor);
                    jedis.set("segmentSummaryList", "[]");
                    jedis.set("segmentQuestionCursor", cursor);
                    jedis.set("segmentQuestionList", "[]");
                    jedis.set("segmentUnderstandCursor", cursor);
                    jedis.set("segmentUnderstandList", "[]");
                    jedis.set("timeSlicedSummaryCursor", cursor);
                    jedis.set("timeSlicedSummaryList", "[]");
                    jedis.set("keyWordCursor", cursor);
                    jedis.set("keyWordList", "[]");
                    jedis.set("keySentenceCursor", cursor);
                    jedis.set("keySentenceList", "[]");
                    jedis.set("questionAnswerList", "[]");
                    jedis.set("backgroundList","[]");
                    break;
                }
            }
        }
    }

    public DiscussInfo getDiscussInfo(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String discussName = jedis.get("discussName");
                    DiscussInfo discussInfo = new DiscussInfo();
                    discussInfo.setDiscussId(discussId);
                    discussInfo.setDiscussName(discussName);

                    // 将startTimeList和stopTimeList从JSON字符串转换为List<String>
                    String startTimeListJson = jedis.get("startTimeList");
                    String stopTimeListJson = jedis.get("stopTimeList");

                    List<String> startTimeList = startTimeListJson == null || startTimeListJson.isEmpty() ?
                            new ArrayList<>() : new Gson().fromJson(startTimeListJson, new TypeToken<List<String>>() {
                    }.getType());
                    List<String> stopTimeList = stopTimeListJson == null || stopTimeListJson.isEmpty() ?
                            new ArrayList<>() : new Gson().fromJson(stopTimeListJson, new TypeToken<List<String>>() {
                    }.getType());

                    // 设置DiscussInfo中的startTimeList和stopTimeList
                    discussInfo.setStartTimeList(startTimeList);
                    discussInfo.setStopTimeList(stopTimeList);

                    // 处理其他相关信息
                    MicSentences micSentences = new MicSentences();
                    Sentences externMicSentences = new Gson().fromJson(jedis.get("externMicSentences"), Sentences.class);
                    Sentences wireMicSentences = new Gson().fromJson(jedis.get("wireMicSentences"), Sentences.class);
                    Sentences virtualMicSentences = new Gson().fromJson(jedis.get("virtualMicSentences"), Sentences.class);
                    micSentences.setExternMicSentences(externMicSentences);
                    micSentences.setWireMicSentences(wireMicSentences);
                    micSentences.setVirtualMicSentences(virtualMicSentences);
                    discussInfo.setMicSentences(micSentences);
                    String discussStatus = jedis.get("discussStatus");
                    if (discussStatus != null) {
                        discussInfo.setDiscussStatus(Integer.parseInt(discussStatus));

                    }

                    Cursor segmentSummaryCursor = new Gson().fromJson(jedis.get("segmentSummaryCursor"), Cursor.class);
                    discussInfo.setSegmentSummaryCursor(segmentSummaryCursor);

                    List<String> segmentSummaryList = new Gson().fromJson(jedis.get("segmentSummaryList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setSegmentSummaryList(segmentSummaryList);

                    Cursor segmentQuestionCursor = new Gson().fromJson(jedis.get("segmentQuestionCursor"), Cursor.class);
                    discussInfo.setSegmentQuestionCursor(segmentQuestionCursor);

                    List<String> segmentQuestionList = new Gson().fromJson(jedis.get("segmentQuestionList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setSegmentQuestionList(segmentQuestionList);

                    Cursor segmentUnderstandCursor = new Gson().fromJson(jedis.get("segmentUnderstandCursor"), Cursor.class);
                    discussInfo.setSegmentUnderstandCursor(segmentUnderstandCursor);

                    List<String> segmentUnderstandList = new Gson().fromJson(jedis.get("segmentUnderstandList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setSegmentUnderstandList(segmentUnderstandList);

                    Cursor timeSlicedSummaryCursor = new Gson().fromJson(jedis.get("timeSlicedSummaryCursor"), Cursor.class);
                    discussInfo.setTimeSlicedSummaryCursor(timeSlicedSummaryCursor);

                    List<String> timeSlicedSummaryList = new Gson().fromJson(jedis.get("timeSlicedSummaryList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setTimeSlicedSummaryList(timeSlicedSummaryList);

                    Cursor keyWordCursor = new Gson().fromJson(jedis.get("keyWordCursor"), Cursor.class);
                    discussInfo.setKeyWordCursor(keyWordCursor);
                    List<String> keyWordList = new Gson().fromJson(jedis.get("keyWordList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setKeyWordList(keyWordList);

                    Cursor keySentenceCursor = new Gson().fromJson(jedis.get("keySentenceCursor"), Cursor.class);
                    discussInfo.setKeySentenceCursor(keySentenceCursor);
                    List<String> keySentenceList = new Gson().fromJson(jedis.get("keySentenceList"), new TypeToken<List<String>>() {
                    }.getType());
                    discussInfo.setKeySentenceList(keySentenceList);

                    List<QuestionAnswer> questionAnswerList = new Gson().fromJson(jedis.get("questionAnswerList"), new TypeToken<List<QuestionAnswer>>() {
                    }.getType());
                    discussInfo.setQuestionAnswerList(questionAnswerList);

                    String backgroundListJson = jedis.get("backgroundList");
                    discussInfo.setBackgroundList(new Gson().fromJson(backgroundListJson, new TypeToken<List<String>>(){}.getType()));
                    return discussInfo;
                }
            }
            return null;
        }
    }

    public void clearDiscuss(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.flushDB();
                    break;
                }
            }
        }
    }

    public void AddMicSentence(String discussId, MicTypeEnum micTypeEnum, Sentence sentence) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String micName;
                    if (micTypeEnum == MicTypeEnum.EXTERN) {
                        micName = "extern";
                    } else if (micTypeEnum == MicTypeEnum.WIRE) {
                        micName = "wire";
                    } else {
                        micName = "virtual";
                    }
                    String key = micName + "MicSentences";
                    Sentences sentences;
                    String sentencesJson = jedis.get(key);
                    if (sentencesJson != null && !sentencesJson.isEmpty()) {
                        // 如果 sentencesJson 不为 null,则解析为 Sentences 对象
                        sentences = new Gson().fromJson(sentencesJson, Sentences.class);
                    } else {
                        // 如果 sentencesJson 为 null,则创建一个新的 Sentences 对象
                        sentences = new Sentences();
                    }
                    sentences.addSentence(sentence);
                    jedis.set(key, new Gson().toJson(sentences));
                    break;
                }
            }
        }
    }

    public String getExternMicSentences(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 直接返回对应的字符串数据
                    return jedis.get("externMicSentences");
                }
            }
            return "";
        }
    }

    public String getWireMicSentences(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 直接返回对应的字符串数据
                    return jedis.get("wireMicSentences");
                }
            }
            return "";
        }
    }

    public String getVirtualMicSentences(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 直接返回对应的字符串数据
                    return jedis.get("virtualMicSentences");
                }
            }
            return "";
        }
    }


    public void updateStartTimeList(String discussId, List<String> startTimeList) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 将startTimeList转换为JSON字符串
                    String startTimeListJson = new Gson().toJson(startTimeList);
                    // 更新Redis中的startTimeList
                    jedis.set("startTimeList", startTimeListJson);
                    break;
                }
            }
        }
    }

    public void updateStopTimeList(String discussId, List<String> stopTimeList) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 将stopTimeList转换为JSON字符串
                    String stopTimeListJson = new Gson().toJson(stopTimeList);
                    // 更新Redis中的stopTimeList
                    jedis.set("stopTimeList", stopTimeListJson);
                    break;
                }
            }
        }
    }

    public void updateDiscussStatus(String discussId, DiscussStatusEnum discussStatusEnum) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("discussStatus", String.valueOf(discussStatusEnum.getValue()));
                    break;
                }
            }
        }
    }

    public Cursor getSegmentSummaryCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("segmentSummaryCursor"), Cursor.class);
                }
            }
            return null;
        }
    }

    public Cursor getTimeSlicedSummaryCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("timeSlicedSummaryCursor"), Cursor.class);
                }
            }
            return null;
        }
    }


    public void setSegmentSummaryCursor(String discussId, Cursor segmentSummaryCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("segmentSummaryCursor", new Gson().toJson(segmentSummaryCursor));
                    break;
                }
            }
        }
    }

    public void setTimeSlicedSummaryCursor(String discussId, Cursor timeSlicedSummaryCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("timeSlicedSummaryCursor", new Gson().toJson(timeSlicedSummaryCursor));
                    break;
                }
            }
        }
    }

    public void addSegmentSummary(String discussId, String segmentSummary) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 获取 segmentSummaryList 的 JSON 字符串
                    List<String> segmentSummaryList = new Gson().fromJson(jedis.get("segmentSummaryList"), new TypeToken<List<String>>() {
                    }.getType());

                    segmentSummaryList.add(segmentSummary);

                    jedis.set("segmentSummaryList", new Gson().toJson(segmentSummaryList));

                    break;
                }
            }
        }
    }

    public void addTimeSlicedSummary(String discussId, String timeSlicedSummary) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<String> timeSlicedSummaryList = new Gson().fromJson(jedis.get("timeSlicedSummaryList"), new TypeToken<List<String>>() {
                    }.getType());

                    timeSlicedSummaryList.add(timeSlicedSummary);

                    jedis.set("timeSlicedSummaryList", new Gson().toJson(timeSlicedSummaryList));

                    break;
                }
            }
        }
    }

    public Cursor getKeyWordCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("keyWordCursor"), Cursor.class);
                }
            }
            return null;
        }
    }

    public Cursor getKeySentenceCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("keySentenceCursor"), Cursor.class);
                }
            }
            return null;
        }
    }

    public void setKeyWordCursor(String discussId, Cursor keyWordCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("keyWordCursor", new Gson().toJson(keyWordCursor));
                    break;
                }
            }
        }
    }

    public void setKeySentenceCursor(String discussId, Cursor keySentenceCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("keySentenceCursor", new Gson().toJson(keySentenceCursor));
                    break;
                }
            }
        }
    }

    public void addKeyWordList(String discussId, List<String> newKeyWordList) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<String> keyWordList = new Gson().fromJson(jedis.get("keyWordList"), new TypeToken<List<String>>() {
                    }.getType());

                    keyWordList.addAll(newKeyWordList);
                    jedis.set("keyWordList", new Gson().toJson(keyWordList));
                    break;
                }
            }
        }
    }

    public void addKeySentenceList(String discussId, List<String> newKeySentenceList) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<String> keySentenceList = new Gson().fromJson(jedis.get("keySentenceList"), new TypeToken<List<String>>() {
                    }.getType());

                    keySentenceList.addAll(newKeySentenceList);
                    jedis.set("keySentenceList", new Gson().toJson(keySentenceList));
                    break;
                }
            }
        }
    }

    public List<QuestionAnswer> getQuestionAnswerList(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("questionAnswerList"), new TypeToken<List<QuestionAnswer>>() {
                    }.getType());
                }
            }
            return null;
        }
    }

    public void addQuestionAnswer(String discussId, QuestionAnswer questionAnswer) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<QuestionAnswer> questionAnswerList = new Gson().fromJson(jedis.get("questionAnswerList"), new TypeToken<List<QuestionAnswer>>() {
                    }.getType());

                    questionAnswerList.add(questionAnswer);
                    jedis.set("questionAnswerList", new Gson().toJson(questionAnswerList));
                    break;
                }
            }
        }
    }

    /**
     * 将指定类型的Sentences对象序列化为JSON字符串并存储到Redis中
     * @param discussId 讨论ID
     * @param micType 麦克风类型
     * @param sentences Sentences对象
     */
    public void setMicSentences(String discussId, MicTypeEnum micType, Sentences sentences) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String key;
                    switch (micType) {
                        case EXTERN:
                            key = "externMicSentences";
                            break;
                        case WIRE:
                            key = "wireMicSentences";
                            break;
                        case VIRTUAL:
                            key = "virtualMicSentences";
                            break;
                        default:
                            throw new IllegalArgumentException("未知的麦克风类型: " + micType);
                    }
                    jedis.set(key, new Gson().toJson(sentences));
                    break;
                }
            }
        }
    }

    public List<String> getBackground(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 从Redis中获取backgroundList,并转换为List<String>返回
                    String backgroundListJson = jedis.get("backgroundList");
                    return new Gson().fromJson(backgroundListJson, new TypeToken<List<String>>(){}.getType());
                }
            }
            // 如果没有找到对应的discussId,返回一个空列表
            return new ArrayList<>();
        }
    }

    public void setBackground(String discussId, List<String> backgroundList) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    // 将backgroundList转换为JSON字符串,并保存到Redis中
                    String backgroundListJson = new Gson().toJson(backgroundList);
                    jedis.set("backgroundList", backgroundListJson);
                    break;
                }
            }
        }
    }

    public Cursor getSegmentQuestionCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("segmentQuestionCursor"), Cursor.class);
                }
            }
            return null;
        }
    }

    public void addSegmentQuestion(String discussId, String segmentQuestion) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<String> segmentQuestionList = new Gson().fromJson(jedis.get("segmentQuestionList"), new TypeToken<List<String>>() {
                    }.getType());

                    segmentQuestionList.add(segmentQuestion);

                    jedis.set("segmentQuestionList", new Gson().toJson(segmentQuestionList));

                    break;
                }
            }
        }
    }

    public void setSegmentQuestionCursor(String discussId, Cursor segmentQuestionCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("segmentQuestionCursor", new Gson().toJson(segmentQuestionCursor));
                    break;
                }
            }
        }
    }

    public Cursor getSegmentUnderstandCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    return new Gson().fromJson(jedis.get("segmentQuestionCursor"), Cursor.class);
                }
            }
            return null;
        }
    }

    public void addSegmentUnderstand(String discussId, String segmentUnderstand) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    List<String> segmentUnderstandList = new Gson().fromJson(jedis.get("segmentUnderstandList"), new TypeToken<List<String>>() {
                    }.getType());

                    segmentUnderstandList.add(segmentUnderstand);

                    jedis.set("segmentUnderstandList", new Gson().toJson(segmentUnderstandList));

                    break;
                }
            }
        }
    }

    public void setSegmentUnderstandCursor(String discussId, Cursor segmentUnderstandCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("segmentUnderstandCursor", new Gson().toJson(segmentUnderstandCursor));
                    break;
                }
            }
        }
    }
}