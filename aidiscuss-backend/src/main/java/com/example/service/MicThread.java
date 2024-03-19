package com.example.service;

import com.example.model.MicAndTranscriber;
import com.example.model.MicSwitchInfo;
import com.example.model.Sentence;
import com.example.model.Sentences;
import com.google.gson.Gson;

// MicThread 类,代表单个麦克风的控制线程
class MicThread extends Thread {
    private String discussId;
    private MicAndTranscriber micAndTranscriber;
    private String micName;
    private MicTranscriberService micTranscriberService; // 添加 MicTranscriberService 实例
    private RedisService redisService;
    private volatile boolean running = true;

    public MicThread(String discussId, MicAndTranscriber micAndTranscriber, String micName) {
        this.discussId = discussId;
        this.micAndTranscriber = micAndTranscriber;
        this.micName = micName;
        this.redisService = RedisService.getInstance();
        this.micTranscriberService = new MicTranscriberService(); // 初始化 MicTranscriberService
    }

    @Override
    public void run() {
        while (running) {
            // 从 redisService 中获取对应的 MicSwitchInfo
            MicSwitchInfo micSwitchInfo = redisService.getMicSwitchInfo(discussId);
            System.out.println(discussId+ ' '+ new Gson().toJson(micSwitchInfo));

            if (micSwitchInfo != null) {
                // 根据 micName 判断是否需要开启或关闭麦克风
                boolean micState;
                switch (micName) {
                    case "externMic":
                        micState = micSwitchInfo.isExternMic();
                        break;
                    case "wireMic":
                        micState = micSwitchInfo.isWireMic();
                        break;
                    case "virtualMic":
                        micState = micSwitchInfo.isVirtualMic();
                        break;
                    default:
                        micState = false;
                }

                // 根据麦克风状态调用 MicAndTranscriber 的方法
                if (micState) {
                    try {
                        micTranscriberService.startMic(micAndTranscriber);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        micTranscriberService.stopMic(micAndTranscriber);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            Sentence sentence = micAndTranscriber.getSentences().popSentence();
            System.out.println("获取");
            if (sentence != null) {
                System.out.println("获取成功");
                redisService.AddMicSentence(sentence, micName, discussId);
            }

            // 休眠1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeMic() throws Exception {
        running = false;
        micTranscriberService.closeMic(micAndTranscriber);
    }
}