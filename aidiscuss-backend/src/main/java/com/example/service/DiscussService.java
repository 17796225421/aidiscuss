package com.example.service;

import com.example.model.DiscussInfo;
import com.example.model.MicSwitchInfo;
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

    /**
     * 更新指定discussId对应的麦克风开关信息
     *
     * @param discussInfo 讨论信息
     */
    public void micSwitch(DiscussInfo discussInfo) {
        String discussId = discussInfo.getDiscussId();
        // 在更新之前，将除了当前discussId所在的库外的所有库的micSwitchInfo的布尔值都设置为false
        redisService.updateOtherMicSwitchInfoToFalse(discussId);

        MicSwitchInfo micSwitchInfo = discussInfo.getMicSwitchInfo();
        // 更新指定discussId的麦克风开关信息
        redisService.updateMicSwitchInfo(discussId, micSwitchInfo);
    }

    public String getExternMicSentences(String discussId) {
        // 直接调用RedisService的方法获取数据
        return RedisService.getInstance().getExternMicSentences(discussId);
    }
}