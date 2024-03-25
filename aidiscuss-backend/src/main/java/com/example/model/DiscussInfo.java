package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class DiscussInfo {
    String discussId;
    String discussName;
    MicSentencestmp micSentencestmp;
    private List<String> startTimeList;
    private List<String> stopTimeList;
    int discussStatus;
    private Cursor segmentSummaryCursor;
    private Cursor timeSlicedSummaryCursor;
    private Cursor segmentQuestionCursor;
    private Cursor segmentUnderstandCursor;
    private List<String> segmentSummaryList;
    private List<String> segmentQuestionList;
    private List<String> segmentUnderstandList;
    private List<String> timeSlicedSummaryList;
    private Cursor keyWordCursor;
    private Cursor keySentenceCursor;
    private List<String> keyWordList;
    private List<String> keySentenceList;
    private List<QuestionAnswer> questionAnswerList;
    private List<String> backgroundList;

    public DiscussInfo() {
        startTimeList = new ArrayList<>();
        stopTimeList = new ArrayList<>();
        segmentSummaryList = new ArrayList<>();
        segmentQuestionList=new ArrayList<>();
        segmentUnderstandList=new ArrayList<>();
        timeSlicedSummaryList = new ArrayList<>();
        keyWordList = new ArrayList<>();
        keySentenceList = new ArrayList<>();
        questionAnswerList = new ArrayList<>();
        backgroundList = new ArrayList<>();
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

    public MicSentencestmp getMicSentences() {
        return micSentencestmp;
    }

    public void setMicSentences(MicSentencestmp micSentencestmp) {
        this.micSentencestmp = micSentencestmp;
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

    public List<String> getBackgroundList() {
        return backgroundList;
    }

    public void setBackgroundList(List<String> backgroundList) {
        this.backgroundList = backgroundList;
    }

    public Cursor getSegmentQuestionCursor() {
        return segmentQuestionCursor;
    }

    public void setSegmentQuestionCursor(Cursor segmentQuestionCursor) {
        this.segmentQuestionCursor = segmentQuestionCursor;
    }

    public List<String> getSegmentQuestionList() {
        return segmentQuestionList;
    }

    public void setSegmentQuestionList(List<String> segmentQuestionList) {
        this.segmentQuestionList = segmentQuestionList;
    }

    public Cursor getSegmentUnderstandCursor() {
        return segmentUnderstandCursor;
    }

    public void setSegmentUnderstandCursor(Cursor segmentUnderstandCursor) {
        this.segmentUnderstandCursor = segmentUnderstandCursor;
    }

    public List<String> getSegmentUnderstandList() {
        return segmentUnderstandList;
    }

    public void setSegmentUnderstandList(List<String> segmentUnderstandList) {
        this.segmentUnderstandList = segmentUnderstandList;
    }
}
