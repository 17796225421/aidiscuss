package com.example.service;

import com.example.model.*;
import com.example.thread.DiscussThread;
import com.example.util.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ManageService {

    private RedisService redisService;
    private Map<String, DiscussThread> discussThreadMap;

    public ManageService() {
        redisService = new RedisService();
        discussThreadMap = new HashMap<>(); // 初始化 HashMap
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
    public DiscussBaseInfo createDiscuss() throws Exception {
        // 获取当前时间，并格式化为指定格式
        LocalDateTime now = LocalDateTime.now();
        String discussName = TimeUtils.getCurrentFormattedTime();
        String discussId = UUID.randomUUID().toString().replace("-", "");

        DiscussInfo discussInfo = new DiscussInfo();
        discussInfo.setDiscussId(discussId);
        discussInfo.setDiscussName(discussName);
        discussInfo.setDiscussStatus(DiscussStatusEnum.CREATED.getValue());
        redisService.createDiscuss(discussInfo);

        // 在项目的根目录下创建一个data文件夹,用来存放数据文件
        String baseDir = "data/";

        // 这里用replace方法进行替换
        String formattedDiscussName = discussInfo.getDiscussName().replace(" ", "_").replace(".", "-").replace(":", "-");

        // 使用修改后的formattedDiscussName作为文件夹名
        String dirName = baseDir + formattedDiscussName;
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new DiscussBaseInfo(discussId, discussName, DiscussStatusEnum.STARTED.getValue());
    }

    public void startDiscuss(String discussId) {
        List<String> allDiscussId = redisService.getAllDiscussId();
        for (String id : allDiscussId) {
            stopDiscuss(id);
        }

        while (redisService.getStopTimeList(discussId).size() + 1 < redisService.getStartTimeList(discussId).size()) {
            redisService.addStopTime(discussId, "-1");
        }
        redisService.addStartTime(discussId, TimeUtils.getCurrentFormattedTime());
        redisService.updateDiscussStatus(discussId,DiscussStatusEnum.STARTED);

        DiscussThread discussThread = new DiscussThread(discussId);
        discussThread.start();
        discussThreadMap.put(discussId, discussThread);
    }

    public void stopDiscuss(String discussId) {
        if (redisService.getStopTimeList(discussId).size() < redisService.getStartTimeList(discussId).size()) {
            redisService.addStopTime(discussId, TimeUtils.getCurrentFormattedTime());
        }
        redisService.updateDiscussStatus(discussId, DiscussStatusEnum.STOPED);

        DiscussThread discussThread = discussThreadMap.get(discussId);
        if (discussThread != null) {
            discussThread.stop();
            discussThreadMap.remove(discussId);
        }
    }

    public void closeDiscuss(String discussId) {
        stopDiscuss(discussId);
        redisService.updateDiscussStatus(discussId, DiscussStatusEnum.CLOSED);
        DiscussInfo discussInfo = redisService.getDiscussInfo(discussId);
        if (discussInfo != null) {
            // 将这个库清空
            redisService.clearDiscuss(discussId);

            // 将DiscussInfo保存到文件中
            saveDiscussInfoToFile(discussInfo);
        }
    }

    private void saveDiscussInfoToFile(DiscussInfo discussInfo) {
        // 在项目的根目录下创建一个data文件夹,用来存放数据文件
        String baseDir = "data/";

        // 这里用replace方法进行替换
        String formattedDiscussName = discussInfo.getDiscussName().replace(" ", "_").replace(".", "-").replace(":", "-");

        // 使用修改后的formattedDiscussName作为文件夹名
        String dirName = baseDir + formattedDiscussName;
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 在formattedDiscussName文件夹下创建一个json文件,存放DiscussInfo的数据
        String fileName = dirName + "/discussInfo.json";
        try (PrintWriter out = new PrintWriter(fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(discussInfo);
            out.write(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}