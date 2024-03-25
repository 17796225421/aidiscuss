package com.example.thread;

import com.example.model.MicTypeEnum;

public class DiscussThread {
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;
    private SegmentSummaryThread segmentSummaryThread;
    private SegmentQuestionThread segmentQuestionThread;
    private TimeSlicedSummaryThread timeSlicedSummaryThread;
    private KeyWordThread keyWordThread;
    private KeySentenceThread keySentenceThread;
    private SentenceSummaryThread sentenceSummaryThread;

    public DiscussThread(String discussId) {
        this.externMicThread = new MicThread(discussId, MicTypeEnum.EXTERN);
        this.wireMicThread = new MicThread(discussId, MicTypeEnum.WIRE);
        this.virtualMicThread = new MicThread(discussId, MicTypeEnum.VIRTUAL);
        this.segmentSummaryThread = new SegmentSummaryThread(discussId);
        this.segmentQuestionThread = new SegmentQuestionThread(discussId);
        this.timeSlicedSummaryThread = new TimeSlicedSummaryThread(discussId);
        this.keyWordThread=new KeyWordThread(discussId);
        this.keySentenceThread=new KeySentenceThread(discussId);
        this.sentenceSummaryThread=new SentenceSummaryThread(discussId);
    }


    public void start() {
        externMicThread.start();
        wireMicThread.start();
        virtualMicThread.start();
        segmentSummaryThread.start();
        segmentQuestionThread.start();
        timeSlicedSummaryThread.start();
        keyWordThread.start();
        keySentenceThread.start();
        sentenceSummaryThread.start();
    }

    public void stop() {
        externMicThread.stopRunning();
        wireMicThread.stopRunning();
        virtualMicThread.stopRunning();
        segmentSummaryThread.stopRunning();
        segmentQuestionThread.stopRunning();
        timeSlicedSummaryThread.stopRunning();
        keyWordThread.stopRunning();
        keySentenceThread.stopRunning();
        sentenceSummaryThread.stopRunning();
    }
}
