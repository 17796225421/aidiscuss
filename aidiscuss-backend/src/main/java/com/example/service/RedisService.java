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
                    jedis.set("externCursor", "0");
                    jedis.set("wireCursor", "0");
                    jedis.set("virtualCursor", "0");
                    jedis.set("segmentSummaryList", "[]");
                    jedis.set("timeSlicedSummaryList", "[]");
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
                    String externCursor = jedis.get("externCursor");
                    String wireCursor = jedis.get("wireCursor");
                    String virtualCursor = jedis.get("virtualCursor");
                    discussInfo.setExternCursor(Integer.parseInt(externCursor));
                    discussInfo.setWireCursor(Integer.parseInt(wireCursor));
                    discussInfo.setVirtualCursor(Integer.parseInt(virtualCursor));

                    String segmentSummaryListJson = jedis.get("segmentSummaryList");
                    List<String> segmentSummaryList = new Gson().fromJson(segmentSummaryListJson, new TypeToken<List<String>>(){}.getType());
                    discussInfo.setSegmentSummaryList(segmentSummaryList);

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

    /**
     * 获取externCursor的值
     *
     * @param discussId 会议ID
     * @return externCursor的值
     */
    public int getExternCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String externCursor = jedis.get("externCursor");
                    return externCursor != null ? Integer.parseInt(externCursor) : 0;
                }
            }
            return 0;
        }
    }

    /**
     * 获取wireCursor的值
     *
     * @param discussId 会议ID
     * @return wireCursor的值
     */
    public int getWireCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String wireCursor = jedis.get("wireCursor");
                    return wireCursor != null ? Integer.parseInt(wireCursor) : 0;
                }
            }
            return 0;
        }
    }

    /**
     * 获取virtualCursor的值
     *
     * @param discussId 会议ID
     * @return virtualCursor的值
     */
    public int getVirtualCursor(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String virtualCursor = jedis.get("virtualCursor");
                    return virtualCursor != null ? Integer.parseInt(virtualCursor) : 0;
                }
            }
            return 0;
        }
    }

    /**
     * 设置externCursor的值
     *
     * @param discussId    会议ID
     * @param externCursor externCursor的值
     */
    public void setExternCursor(String discussId, int externCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("externCursor", String.valueOf(externCursor));
                    break;
                }
            }
        }
    }

    /**
     * 设置wireCursor的值
     *
     * @param discussId  会议ID
     * @param wireCursor wireCursor的值
     */
    public void setWireCursor(String discussId, int wireCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("wireCursor", String.valueOf(wireCursor));
                    break;
                }
            }
        }
    }

    /**
     * 设置virtualCursor的值
     *
     * @param discussId     会议ID
     * @param virtualCursor virtualCursor的值
     */
    public void setVirtualCursor(String discussId, int virtualCursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("virtualCursor", String.valueOf(virtualCursor));
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
                    String segmentSummaryListJson = jedis.get("segmentSummaryList");

                    // 将 JSON 字符串转换为 List<String>
                    List<String> segmentSummaryList = new Gson().fromJson(segmentSummaryListJson, new TypeToken<List<String>>(){}.getType());

                    // 将新的 segmentSummary 添加到 List 的末尾
                    segmentSummaryList.add(segmentSummary);

                    // 将更新后的 List 转换为 JSON 字符串
                    String updatedSegmentSummaryListJson = new Gson().toJson(segmentSummaryList);

                    // 更新 Redis 中的 segmentSummaryList
                    jedis.set("segmentSummaryList", updatedSegmentSummaryListJson);

                    break;
                }
            }
        }
    }
}