package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
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

    public List<DiscussBaseInfo> getDiscussBaseInfoList() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<DiscussBaseInfo> discussBaseInfoList = new ArrayList<>();
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussName")) {
                    String discussName = jedis.get("discussName");
                    String discussId = jedis.get("discussId");
                    DiscussBaseInfo discussBaseInfo = new DiscussBaseInfo(discussId, discussName);
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
                    jedis.set("micSwitchInfo", new Gson().toJson(discussInfo.getMicSwitchInfo()));
                    jedis.set("externMicSentences", "");
                    jedis.set("wireMicSentences", "");
                    jedis.set("virtualMicSentences", "");
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
                    String micSwitchInfo = jedis.get("micSwitchInfo");
                    DiscussInfo discussInfo = new DiscussInfo();
                    discussInfo.setDiscussId(discussId);
                    discussInfo.setDiscussName(discussName);
                    discussInfo.setMicSwitchInfo(new Gson().fromJson(micSwitchInfo, MicSwitchInfo.class));
                    MicSentences micSentences = new MicSentences();
                    Sentences externMicSentences = new Gson().fromJson(jedis.get("externMicSentences"), Sentences.class);
                    Sentences wireMicSentences = new Gson().fromJson(jedis.get("wireMicSentences"), Sentences.class);
                    Sentences virtualMicSentences = new Gson().fromJson(jedis.get("virtualMicSentences"), Sentences.class);
                    micSentences.setExternMicSentences(externMicSentences);
                    micSentences.setWireMicSentences(wireMicSentences);
                    micSentences.setVirtualMicSentences(virtualMicSentences);
                    discussInfo.setMicSentences(micSentences);
                    return discussInfo;
                }
            }
            return null;
        }
    }

    public void updateMicSwitchInfo(String discussId, MicSwitchInfo micSwitchInfo) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    jedis.set("micSwitchInfo", new Gson().toJson(micSwitchInfo));
                    break;
                }
            }
        }
    }

    public void updateOtherMicSwitchInfoToFalse(String excludeDiscussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (!jedis.exists("discussId") || !jedis.get("discussId").equals(excludeDiscussId)) {
                    if (jedis.exists("micSwitchInfo")) {
                        MicSwitchInfo micSwitchInfo = new MicSwitchInfo();
                        jedis.set("micSwitchInfo", new Gson().toJson(micSwitchInfo));
                    }
                }
            }
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

    public MicSwitchInfo getMicSwitchInfo(String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    if (jedis.exists("micSwitchInfo")) {
                        MicSwitchInfo micSwitchInfo = new Gson().fromJson(jedis.get("micSwitchInfo"), MicSwitchInfo.class);
                        return micSwitchInfo;
                    }
                }
            }
        }
        return null;
    }

    public void AddMicSentence(Sentence sentence, String micName, String discussId) {
        try (Jedis jedis = jedisPool.getResource()) {
            int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
            for (int i = 0; i < dbCount; i++) {
                jedis.select(i);
                if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                    String key = micName + "Sentences";
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
}