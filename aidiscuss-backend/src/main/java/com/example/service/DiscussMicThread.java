package com.example.service;

import com.example.model.MicAndTranscriber;

// DiscussMicThread 类,用于管理单个会议的麦克风线程
class DiscussMicThread {
    private String discussId;
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;

    public DiscussMicThread(String discussId, MicAndTranscriber externMic, MicAndTranscriber wireMic, MicAndTranscriber virtualMic, MicTranscriberService micTranscriberService) {
        this.discussId = discussId;
        this.externMicThread = new MicThread(discussId, externMic, "externMic",micTranscriberService);
        this.wireMicThread = new MicThread(discussId, wireMic, "wireMic",micTranscriberService);
        this.virtualMicThread = new MicThread(discussId, virtualMic, "virtualMic",micTranscriberService);
    }

    public void start() {
        externMicThread.start();
//        wireMicThread.start();
//        virtualMicThread.start();
    }

    public void closeMics() throws Exception {
        externMicThread.closeMic();
        wireMicThread.closeMic();
        virtualMicThread.closeMic();
    }
}