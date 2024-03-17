package com.example.service;

import com.example.model.DiscussBaseInfo;
import com.example.model.ManageInfo;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RedisService {

    private Jedis jedis;

    public RedisService() {
        // 初始化Jedis连接
        jedis = new Jedis("localhost", 6379);
    }

    /**
     * 遍历Redis的每个库,查找key为discussName的数据,并组装成DiscussBaseInfo列表
     * @return DiscussBaseInfo列表
     */
    public List<DiscussBaseInfo> getDiscussBaseInfoList() {
        List<DiscussBaseInfo> discussBaseInfoList = new ArrayList<>();

        // 获取Redis中的库数量
        int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));

        // 遍历每个库
        for (int i = 0; i < dbCount; i++) {
            // 选择库
            jedis.select(i);

            // 检查当前库中是否存在key为discussName的数据
            if (jedis.exists("discussName")) {
                String discussName = jedis.get("discussName");
                DiscussBaseInfo discussBaseInfo = new DiscussBaseInfo();
                discussBaseInfo.setRedisId(i);
                discussBaseInfo.setDiscussName(discussName);
                discussBaseInfoList.add(discussBaseInfo);
            }
        }

        return discussBaseInfoList;
    }

    /**
     * 创建新的讨论
     * @param discussName 讨论名称
     */
    public void createDiscuss(String discussName) {
        // 获取Redis中的库数量
        int dbCount = Integer.parseInt(jedis.configGet("databases").get(1));

        // 遍历每个库,找到一个未使用的库
        for (int i = 0; i < dbCount; i++) {
            // 选择库
            jedis.select(i);

            // 检查当前库是否为空
            if (jedis.dbSize() == 0) {
                // 如果当前库为空,则将discussName作为key,当前时间戳作为value存入Redis
                jedis.set("discussName", discussName);
                break;
            }
        }
    }
}