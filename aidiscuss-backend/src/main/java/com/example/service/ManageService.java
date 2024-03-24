package com.example.service;

import com.example.model.*;
import com.example.util.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ManageService {

    private RedisService redisService;
    private Map<String, DiscussMicThread> discussMicThreadMap; // 用于保存 discussId 和对应的麦克风线程

    public ManageService() {
        redisService = RedisService.getInstance();
        discussMicThreadMap = new HashMap<>(); // 初始化 HashMap
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
        // 创建新的Redis库，并添加discussName作为key，当前时间作为value
        redisService.createDiscuss(discussInfo);

        return new DiscussBaseInfo(discussId, discussName, DiscussStatusEnum.STARTED.getValue());
    }

    public void startDiscuss(String discussId) {
        List<String> allDiscussId = redisService.getAllDiscussId();
        for (String id : allDiscussId) {
            stopDiscuss(id);
        }

        DiscussInfo discussInfo = redisService.getDiscussInfo(discussId);
        // 记录开始时间
        String startTime = TimeUtils.getCurrentFormattedTime();
        discussInfo.getStartTimeList().add(startTime);

        // 检查停止时间列表长度
        List<String> stopTimeList = discussInfo.getStopTimeList();
        List<String> startTimeList = discussInfo.getStartTimeList();
        while (stopTimeList.size() < startTimeList.size() - 1) {
            stopTimeList.add("-1");
        }
        redisService.updateStartTimeList(discussId, startTimeList);
        redisService.updateStopTimeList(discussId, stopTimeList);
        redisService.updateDiscussStatus(discussId, DiscussStatusEnum.STARTED);

        // 创建 DiscussMicThread 实例,并启动线程
        DiscussMicThread discussMicThread = new DiscussMicThread(discussId);
        discussMicThread.start();

        // 将 discussId 和对应的 DiscussMicThread 实例保存到 HashMap 中
        discussMicThreadMap.put(discussId, discussMicThread);
    }

    public void stopDiscuss(String discussId) {
        DiscussInfo discussInfo = redisService.getDiscussInfo(discussId);
        List<String> startTimeList = discussInfo.getStartTimeList();
        List<String> stopTimeList = discussInfo.getStopTimeList();
        if (stopTimeList.size() < startTimeList.size()) {
            // 记录停止时间
            String stopTime = TimeUtils.getCurrentFormattedTime();
            stopTimeList.add(stopTime);
            redisService.updateStopTimeList(discussId, stopTimeList);
        }
        redisService.updateDiscussStatus(discussId, DiscussStatusEnum.STOPED);

        // 从 HashMap 中获取对应的 DiscussMicThread 实例,并关闭麦克风
        DiscussMicThread discussMicThread = discussMicThreadMap.get(discussId);
        if (discussMicThread != null) {
            discussMicThread.stop();
            discussMicThreadMap.remove(discussId); // 从 HashMap 中移除
        }
    }

    public void closeDiscuss(String discussId) {
        stopDiscuss(discussId);
        redisService.updateDiscussStatus(discussId, DiscussStatusEnum.CLOSED);
        // 先从RedisService找到discussId对应的库,读取所有的信息,构造DiscussInfo
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
        String baseDir = "data/discuss/closed/";

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