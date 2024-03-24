package com.example.thread;

public class DiscussSummaryThread {
    private SegmentSummaryThread segmentSummaryThread;
    private TimeSlicedSummaryThread timeSlicedSummaryThread;

    public DiscussSummaryThread(String discussId) {
        this.segmentSummaryThread = new SegmentSummaryThread(discussId);
        this.timeSlicedSummaryThread=new TimeSlicedSummaryThread(discussId);
    }

    public void start(){
        segmentSummaryThread.start();
        timeSlicedSummaryThread.start();
    }

    public void stop(){
        segmentSummaryThread.stopRunning();
        timeSlicedSummaryThread.stopRunning();
    }
}
