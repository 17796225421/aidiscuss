package com.example.service;

import com.example.model.DiscussBaseInfo;
import com.example.model.DiscussInfo;
import com.example.model.ManageInfo;
import com.example.model.MicSwitchInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
        String discussId = UUID.randomUUID().toString().replace("-", "");

        DiscussInfo discussInfo = new DiscussInfo();
        discussInfo.setDiscussId(discussId);
        discussInfo.setDiscussName(discussName);
        MicSwitchInfo micSwitchInfo = new MicSwitchInfo();
        discussInfo.setMicSwitchInfo(micSwitchInfo);
        // 创建新的Redis库，并添加discussName作为key，当前时间作为value
        redisService.createDiscuss(discussInfo);

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