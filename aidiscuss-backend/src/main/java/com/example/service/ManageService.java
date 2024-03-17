package com.example.service;

import com.example.model.DiscussBaseInfo;
import com.example.model.ManageInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ManageService {

    private RedisService redisService;

    public ManageService() {
        redisService = new RedisService();
    }

    /**
     * 获取管理信息的方法
     *
     * @return 管理信息对象
     */
    public ManageInfo getManageInfo() {
        ManageInfo manageInfo = new ManageInfo();
        // 调用RedisService的方法获取DiscussBaseInfo列表
        manageInfo.setDiscusses(redisService.getDiscussBaseInfoList());
        return manageInfo;
    }

    /**
     * 创建新的讨论
     *
     * @return 创建的讨论名称
     */
    public DiscussBaseInfo createDiscuss() {
        // 获取当前时间，并格式化为指定格式
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd_HH_mm_ss");
        String discussName = now.format(formatter);
        String discussId = UUID.randomUUID().toString();

        // 创建新的Redis库，并添加discussName作为key，当前时间作为value
        redisService.createDiscuss(discussName, discussId);

        return new DiscussBaseInfo(discussId, discussName);
    }
}