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
     * @param discussInfo 讨论信息
     */
    public void micSwitch(DiscussInfo discussInfo) {
        String discussId = discussInfo.getDiscussId();
        MicSwitchInfo micSwitchInfo = discussInfo.getMicSwitchInfo();
        // 调用RedisService的updateMicSwitchInfo方法更新麦克风开关信息
        redisService.updateMicSwitchInfo(discussId, micSwitchInfo);
    }
}