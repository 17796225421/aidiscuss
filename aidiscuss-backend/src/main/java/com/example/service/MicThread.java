package com.example.service;

import com.example.model.MicTypeEnum;

// MicThread 类,代表单个麦克风的控制线程
class MicThread extends Thread {
    private String discussId;
    private MicTypeEnum micTypeEnum;
    private MicTranscriberService micTranscriberService = new MicTranscriberService();
    private volatile boolean running = true; // 添加 running 标志变量

    public MicThread(String discussId, MicTypeEnum micTypeEnum) {
        this.discussId = discussId;
        this.micTypeEnum = micTypeEnum;
    }

    @Override
    public void run() {
        try {
            micTranscriberService.startMic(discussId, micTypeEnum, this); // 传递 running 标志变量
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stopRunning() {
        running = false; // 设置 running 为 false,通知麦克风线程停止运行
    }

    public boolean isRunning() {
        return running;
    }
}