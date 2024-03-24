package com.example.thread;

import com.example.model.MicTypeEnum;

public class DiscussThread {
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;
    private SegmentSummaryThread segmentSummaryThread;
    private TimeSlicedSummaryThread timeSlicedSummaryThread;

    public DiscussThread(String discussId) {
        this.externMicThread = new MicThread(discussId, MicTypeEnum.EXTERN);
        this.wireMicThread = new MicThread(discussId, MicTypeEnum.WIRE);
        this.virtualMicThread = new MicThread(discussId, MicTypeEnum.VIRTUAL);
        this.segmentSummaryThread = new SegmentSummaryThread(discussId);
        this.timeSlicedSummaryThread = new TimeSlicedSummaryThread(discussId);
    }


    public void start() {
        externMicThread.start();
        wireMicThread.start();
        virtualMicThread.start();
        segmentSummaryThread.start();
        timeSlicedSummaryThread.start();
    }

    public void stop() {
        externMicThread.stopRunning();
        wireMicThread.stopRunning();
        virtualMicThread.stopRunning();
        segmentSummaryThread.stopRunning();
        timeSlicedSummaryThread.stopRunning();
    }
}
