package com.example.service;

import com.example.model.DiscussBaseInfo;
import com.example.model.DiscussInfo;
import com.example.model.MicSwitchInfo;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {

    private Jedis jedis;

    public RedisService() {
        jedis = new Jedis("localhost", 6379);
    }

    public List<DiscussBaseInfo> getDiscussBaseInfoList() {
        List<DiscussBaseInfo> discussBaseInfoList = new ArrayList<>();
        int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
        for (int i = 0; i < dbCount; i++) {
            jedis.select(i);
            if (jedis.exists("discussName")) {
                String discussName = jedis.get("discussName");
                // 假设discussId也是以某种形式存储
                String discussId = jedis.get("discussId"); // 假设在创建时一起存储了 discussId
                DiscussBaseInfo discussBaseInfo = new DiscussBaseInfo(discussId, discussName);
                discussBaseInfoList.add(discussBaseInfo);
            }
        }
        return discussBaseInfoList;
    }

    public void createDiscuss(DiscussInfo discussInfo) {
        int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
        for (int i = 0; i < dbCount; i++) {
            jedis.select(i);
            if (jedis.dbSize() == 0) {
                // 生成一个唯一的 discussId
                // 将discussName和discussId一起存储
                jedis.set("discussId", discussInfo.getDiscussId()); // 存储唯一的 discussId
                jedis.set("discussName", discussInfo.getDiscussName());
                jedis.set("micSwitchInfo", new Gson().toJson(discussInfo.getMicSwitchInfo()));
                break;
            }
        }
    }

    public DiscussInfo getDiscussInfo(String discussId) {
        int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));
        for (int i = 0; i < dbCount; i++) {
            jedis.select(i);
            // 判断当前库中是否存在discussId对应的key,并且值与传入的discussId相等
            if (jedis.exists("discussId") && jedis.get("discussId").equals(discussId)) {
                // 获取discussName
                String discussName = jedis.get("discussName");
                String micSwitchInfo = jedis.get("micSwitchInfo");

                // 创建DiscussInfo对象并设置discussId和discussName
                DiscussInfo discussInfo = new DiscussInfo();
                discussInfo.setDiscussId(discussId);
                discussInfo.setDiscussName(discussName);
                discussInfo.setMicSwitchInfo(new Gson().fromJson(micSwitchInfo, MicSwitchInfo.class));
                return discussInfo;
            }
        }
        // 如果没有找到对应的discussId,返回null
        return null;
    }
}