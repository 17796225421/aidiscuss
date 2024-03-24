package com.example.thread;


public class TimeSlicedSummaryThread extends Thread {
    private String discussId;
    private volatile boolean running = true; // 添加 running 标志变量

    public TimeSlicedSummaryThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {

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
