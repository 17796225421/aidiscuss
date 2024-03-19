package com.example.service;

import com.example.model.MicAndTranscriber;

// DiscussMicThread 类,用于管理单个会议的麦克风线程
class DiscussMicThread {
    private String discussId;
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;

    public DiscussMicThread(String discussId, MicAndTranscriber externMic, MicAndTranscriber wireMic, MicAndTranscriber virtualMic) {
        this.discussId = discussId;
        this.externMicThread = new MicThread(discussId, externMic, "externMic");
        this.wireMicThread = new MicThread(discussId, wireMic, "wireMic");
        this.virtualMicThread = new MicThread(discussId, virtualMic, "virtualMic");
    }

    public void start() {
        externMicThread.start();
        wireMicThread.start();
        virtualMicThread.start();
    }

    public void closeMics() throws Exception {
        externMicThread.closeMic();
        wireMicThread.closeMic();
        virtualMicThread.closeMic();
    }
}