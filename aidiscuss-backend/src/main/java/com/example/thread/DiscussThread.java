package com.example.thread;

import com.example.model.MicTypeEnum;

public class DiscussThread {
    private MicThread externMicThread;
    private MicThread wireMicThread;
    private MicThread virtualMicThread;
    private SegmentSummaryThread segmentSummaryThread;
    private SegmentQuestionThread segmentQuestionThread;
    private SegmentUnderstandThread segmentUnderstandThread;
    private SegmentRemarkThread segmentRemarkThread;
    private SegmentRestateThread segmentRestateThread;
    private SegmentAnalogyThread segmentAnalogyThread;
    private SegmentContinueThread segmentContinueThread;
    private SegmentMultiangleThread segmentMultiangleThread;
    private SegmentLogicThread segmentLogicThread;
    private TimeSlicedSummaryThread timeSlicedSummaryThread;
    private KeyWordThread keyWordThread;
    private KeySentenceThread keySentenceThread;
    private SentenceSummaryThread sentenceSummaryThread;
    private SegmentCorrectThread segmentCorrectThread;

    public DiscussThread(String discussId) {
        this.externMicThread = new MicThread(discussId, MicTypeEnum.EXTERN);
        this.wireMicThread = new MicThread(discussId, MicTypeEnum.WIRE);
        this.virtualMicThread = new MicThread(discussId, MicTypeEnum.VIRTUAL);
        this.segmentSummaryThread = new SegmentSummaryThread(discussId);
        this.segmentQuestionThread = new SegmentQuestionThread(discussId);
        this.segmentUnderstandThread = new SegmentUnderstandThread(discussId);
        this.segmentRemarkThread = new SegmentRemarkThread(discussId);
        this.segmentRestateThread = new SegmentRestateThread(discussId);
        this.segmentAnalogyThread = new SegmentAnalogyThread(discussId);
        this.segmentContinueThread = new SegmentContinueThread(discussId);
        this.segmentMultiangleThread = new SegmentMultiangleThread(discussId);
        this.segmentLogicThread = new SegmentLogicThread(discussId);
        this.timeSlicedSummaryThread = new TimeSlicedSummaryThread(discussId);
        this.keyWordThread=new KeyWordThread(discussId);
        this.keySentenceThread=new KeySentenceThread(discussId);
        this.sentenceSummaryThread=new SentenceSummaryThread(discussId);
        this.segmentCorrectThread=new SegmentCorrectThread(discussId);
    }


    public void start() {
        externMicThread.start();
        wireMicThread.start();
        virtualMicThread.start();
        segmentSummaryThread.start();
        segmentQuestionThread.start();
        segmentUnderstandThread.start();
        segmentRemarkThread.start();
        segmentRestateThread.start();
        segmentAnalogyThread.start();
        segmentContinueThread.start();
        segmentMultiangleThread.start();
        segmentLogicThread.start();
        timeSlicedSummaryThread.start();
        keyWordThread.start();
        keySentenceThread.start();
        sentenceSummaryThread.start();
        segmentCorrectThread.start();
    }

    public void stop() {
        externMicThread.stopRunning();
        wireMicThread.stopRunning();
        virtualMicThread.stopRunning();
        segmentSummaryThread.stopRunning();
        segmentQuestionThread.stopRunning();
        segmentUnderstandThread.stopRunning();
        segmentRemarkThread.stopRunning();
        segmentRestateThread.stopRunning();
        segmentAnalogyThread.stopRunning();
        segmentContinueThread.stopRunning();
        segmentMultiangleThread.stopRunning();
        segmentLogicThread.stopRunning();
        timeSlicedSummaryThread.stopRunning();
        keyWordThread.stopRunning();
        keySentenceThread.stopRunning();
        sentenceSummaryThread.stopRunning();
        segmentCorrectThread.stopRunning();
    }
}
