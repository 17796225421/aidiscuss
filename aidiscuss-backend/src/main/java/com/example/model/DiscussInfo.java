package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class DiscussInfo {
    String discussId;
    String discussName;
    MicSentences micSentences;
    private List<String> startTimeList;
    private List<String> stopTimeList;
    int discussStatus;
    private Cursor segmentSummaryCursor;
    private Cursor timeSlicedSummaryCursor;
    private List<String> segmentSummaryList;
    private List<String> timeSlicedSummaryList;
    private Cursor keyWordCursor;
    private Cursor keySentenceCursor;
    private List<String> keyWordList;
    private List<String> keySentenceList;
    private List<QuestionAnswer>questionAnswerList;

    public DiscussInfo() {
        startTimeList = new ArrayList<>();
        stopTimeList = new ArrayList<>();
        segmentSummaryList = new ArrayList<>();
        timeSlicedSummaryList = new ArrayList<>();
        keyWordList = new ArrayList<>();
        keySentenceList = new ArrayList<>();
        questionAnswerList=new ArrayList<>();
    }

    public String getDiscussId() {
        return discussId;
    }

    public void setDiscussId(String discussId) {
        this.discussId = discussId;
    }

    public String getDiscussName() {
        return discussName;
    }

    public void setDiscussName(String discussName) {
        this.discussName = discussName;
    }

    public MicSentences getMicSentences() {
        return micSentences;
    }

    public void setMicSentences(MicSentences micSentences) {
        this.micSentences = micSentences;
    }

    public List<String> getStartTimeList() {
        return startTimeList;
    }

    public void setStartTimeList(List<String> startTimeList) {
        this.startTimeList = startTimeList;
    }

    public List<String> getStopTimeList() {
        return stopTimeList;
    }

    public void setStopTimeList(List<String> stopTimeList) {
        this.stopTimeList = stopTimeList;
    }

    public int getDiscussStatus() {
        return discussStatus;
    }

    public void setDiscussStatus(int discussStatus) {
        this.discussStatus = discussStatus;
    }

    public Cursor getSegmentSummaryCursor() {
        return segmentSummaryCursor;
    }

    public void setSegmentSummaryCursor(Cursor segmentSummaryCursor) {
        this.segmentSummaryCursor = segmentSummaryCursor;
    }

    public Cursor getTimeSlicedSummaryCursor() {
        return timeSlicedSummaryCursor;
    }

    public void setTimeSlicedSummaryCursor(Cursor timeSlicedSummaryCursor) {
        this.timeSlicedSummaryCursor = timeSlicedSummaryCursor;
    }

    public List<String> getSegmentSummaryList() {
        return segmentSummaryList;
    }

    public void setSegmentSummaryList(List<String> segmentSummaryList) {
        this.segmentSummaryList = segmentSummaryList;
    }

    public List<String> getTimeSlicedSummaryList() {
        return timeSlicedSummaryList;
    }

    public void setTimeSlicedSummaryList(List<String> timeSlicedSummaryList) {
        this.timeSlicedSummaryList = timeSlicedSummaryList;
    }

    public List<String> getKeyWordList() {
        return keyWordList;
    }

    public void setKeyWordList(List<String> keyWordList) {
        this.keyWordList = keyWordList;
    }

    public List<String> getKeySentenceList() {
        return keySentenceList;
    }

    public void setKeySentenceList(List<String> keySentenceList) {
        this.keySentenceList = keySentenceList;
    }

    public Cursor getKeyWordCursor() {
        return keyWordCursor;
    }

    public void setKeyWordCursor(Cursor keyWordCursor) {
        this.keyWordCursor = keyWordCursor;
    }

    public Cursor getKeySentenceCursor() {
        return keySentenceCursor;
    }

    public void setKeySentenceCursor(Cursor keySentenceCursor) {
        this.keySentenceCursor = keySentenceCursor;
    }

    public List<QuestionAnswer> getQuestionAnswerList() {
        return questionAnswerList;
    }

    public void setQuestionAnswerList(List<QuestionAnswer> questionAnswerList) {
        this.questionAnswerList = questionAnswerList;
    }
}
