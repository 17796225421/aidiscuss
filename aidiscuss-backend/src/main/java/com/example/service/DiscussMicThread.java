package com.example.service;

import com.example.model.MicTypeEnum;

// DiscussMicThread 类,用于管理单个会议的麦克风线程
class DiscussMicThread {
    private String discussId;
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;

    public DiscussMicThread(String discussId) {
        this.discussId = discussId;
        this.externMicThread = new MicThread(discussId, MicTypeEnum.EXTERN);
        this.wireMicThread = new MicThread(discussId, MicTypeEnum.WIRE);
        this.virtualMicThread = new MicThread(discussId, MicTypeEnum.VIRTUAL);
    }

    public void start() {
        externMicThread.start();
        wireMicThread.start();
        virtualMicThread.start();
    }

    public void stop() {
        System.out.println(111);
        externMicThread.stopRunning(); // 设置 running 为 false,通知麦克风线程停止运行
        wireMicThread.stopRunning();
        virtualMicThread.stopRunning();
    }
}