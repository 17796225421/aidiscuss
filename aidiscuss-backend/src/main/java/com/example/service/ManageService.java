package com.example.service;

import com.example.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ManageService {

    private RedisService redisService;
    private MicTranscriberService micTranscriberService; // 添加 MicTranscriberService 实例
    private Map<String, DiscussMicThread> discussMicThreadMap; // 用于保存 discussId 和对应的麦克风线程

    public ManageService() {
        redisService = RedisService.getInstance();
        micTranscriberService = new MicTranscriberService(); // 初始化 MicTranscriberService
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_dd_HH_mm_ss");
        String discussName = now.format(formatter);
        String discussId = UUID.randomUUID().toString().replace("-", "");

        DiscussInfo discussInfo = new DiscussInfo();
        discussInfo.setDiscussId(discussId);
        discussInfo.setDiscussName(discussName);
        MicSwitchInfo micSwitchInfo = new MicSwitchInfo();
        discussInfo.setMicSwitchInfo(micSwitchInfo);
        // 创建新的Redis库，并添加discussName作为key，当前时间作为value
        redisService.createDiscuss(discussInfo);

        // 调用 MicTranscriberService 的 openMic 方法,并创建对应的麦克风线程
        MicAndTranscriber externMic = micTranscriberService.openMic("M3");
        MicAndTranscriber wireMic = micTranscriberService.openMic("Realtek");
        MicAndTranscriber virtualMic = micTranscriberService.openMic("B1");

        // 创建 DiscussMicThread 实例,并启动线程
        DiscussMicThread discussMicThread = new DiscussMicThread(discussId, externMic, wireMic, virtualMic,micTranscriberService);
        discussMicThread.start();

        // 将 discussId 和对应的 DiscussMicThread 实例保存到 HashMap 中
        discussMicThreadMap.put(discussId, discussMicThread);

        return new DiscussBaseInfo(discussId, discussName);

    }

    public void closeDiscuss(String discussId) {
        // 先从RedisService找到discussId对应的库,读取所有的信息,构造DiscussInfo
        DiscussInfo discussInfo = redisService.getDiscussInfo(discussId);
        if (discussInfo != null) {
            // 将这个库清空
            redisService.clearDiscuss(discussId);

            // 将DiscussInfo保存到文件中
            saveDiscussInfoToFile(discussInfo);
        }

        // 从 HashMap 中获取对应的 DiscussMicThread 实例,并关闭麦克风
        DiscussMicThread discussMicThread = discussMicThreadMap.get(discussId);
        if (discussMicThread != null) {
            try {
                discussMicThread.closeMics();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            discussMicThreadMap.remove(discussId); // 从 HashMap 中移除
        }
    }

    private void saveDiscussInfoToFile(DiscussInfo discussInfo) {
        // 我觉得可以在项目的根目录下创建一个data文件夹,用来存放数据文件
        String baseDir = "data/discuss/closed/";
        // 使用discussName作为文件夹名
        String dirName = baseDir + discussInfo.getDiscussName();
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 在discussName文件夹下创建一个json文件,存放DiscussInfo的数据
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