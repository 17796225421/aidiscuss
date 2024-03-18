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

    public void micSwitch(DiscussInfo discussInfo) {
        MicSwitchInfo micSwitchInfo = discussInfo.getMicSwitchInfo();
        System.out.println(discussInfo.getDiscussId()+ ' '+ micSwitchInfo.isExternMic());
        // TODO: 具体处理麦克风开关的逻辑
        // 可以根据micSwitchInfo的属性值进行相应的操作
        if (micSwitchInfo.isExternMic()) {
            // 处理外置麦克风开启的逻辑
        }
        if (micSwitchInfo.isWireMic()) {
            // 处理有线麦克风开启的逻辑
        }
        if (micSwitchInfo.isVirtualMic()) {
            // 处理虚拟麦克风开启的逻辑
        }
    }
}