package com.example.service;

import com.example.model.ManageInfo;
import org.springframework.stereotype.Service;

@Service
public class ManageService {

    private RedisService redisService;

    public ManageService() {
        redisService = new RedisService();
    }

    /**
     * 获取管理信息的方法
     * @return 管理信息对象
     */
    public ManageInfo getManageInfo() {
        ManageInfo manageInfo = new ManageInfo();
        // 调用RedisService的方法获取DiscussBaseInfo列表
        manageInfo.setDiscusses(redisService.getDiscussBaseInfoList());
        return manageInfo;
    }
}