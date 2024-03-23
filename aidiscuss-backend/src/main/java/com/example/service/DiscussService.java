package com.example.service;

import com.example.model.DiscussInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscussService {

    @Autowired
    private RedisService redisService;

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
}