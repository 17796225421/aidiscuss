package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class DiscussInfo {
    // 讨论ID
    String discussId;
    // 讨论名称
    String discussName;
    // 句子列表
    List<Sentence> sentenceList;
    // 开始时间列表
    private List<String> startTimeList;
    // 结束时间列表
    private List<String> stopTimeList;
    // 片段总结列表
    private List<String> segmentSummaryList;
    // 片段问题列表
    private List<String> segmentQuestionList;
    // 片段理解列表
    private List<String> segmentUnderstandList;
    private List<String> segmentRemarkList;
    private List<String> segmentRestateList;
    private List<String> segmentAnalogyList;
    private List<String> segmentContinueList;
    private List<String> segmentMultiangleList;
    private List<String> segmentLogicList;
    // 时间切片总结列表
    private List<String> timeSlicedSummaryList;
    // 关键词列表
    private List<String> keyWordList;
    // 关键句列表
    private List<String> keySentenceList;
    // 背景列表
    private List<String> backgroundList;
    // 问答列表
    private List<QuestionAnswer> questionAnswerList;
    // 讨论状态
    int discussStatus;
    // 片段总结游标
    private int segmentSummaryCursor;
    private int segmentCorrectCursor;
    private int sentenceSummaryCursor;
    // 片段问题游标
    private int segmentQuestionCursor;
    // 片段理解游标
    private int segmentUnderstandCursor;
    private int segmentRemarkCursor;
    private int segmentRestateCursor;
    private int segmentAnalogyCursor;
    private int segmentContinueCursor;
    private int segmentMultiangleCursor;
    private int segmentLogicCursor;
    // 时间切片总结游标
    private int timeSlicedSummaryCursor;
    // 关键词游标
    private int keyWordCursor;
    // 关键句游标
    private int keySentenceCursor;

    public DiscussInfo() {
        sentenceList = new ArrayList<>();
        startTimeList = new ArrayList<>();
        stopTimeList = new ArrayList<>();
        segmentSummaryList = new ArrayList<>();
        segmentQuestionList = new ArrayList<>();
        segmentUnderstandList = new ArrayList<>();
        segmentRemarkList = new ArrayList<>();
        segmentRestateList = new ArrayList<>();
        segmentAnalogyList = new ArrayList<>();
        segmentContinueList = new ArrayList<>();
        segmentMultiangleList = new ArrayList<>();
        segmentLogicList = new ArrayList<>();
        timeSlicedSummaryList = new ArrayList<>();
        keyWordList = new ArrayList<>();
        keySentenceList = new ArrayList<>();
        backgroundList = new ArrayList<>();
        questionAnswerList = new ArrayList<>();
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

    public List<String> getSegmentSummaryList() {
        return segmentSummaryList;
    }

    public void setSegmentSummaryList(List<String> segmentSummaryList) {
        this.segmentSummaryList = segmentSummaryList;
    }

    public List<String> getSegmentQuestionList() {
        return segmentQuestionList;
    }

    public void setSegmentQuestionList(List<String> segmentQuestionList) {
        this.segmentQuestionList = segmentQuestionList;
    }

    public List<String> getSegmentUnderstandList() {
        return segmentUnderstandList;
    }

    public void setSegmentUnderstandList(List<String> segmentUnderstandList) {
        this.segmentUnderstandList = segmentUnderstandList;
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

    public List<String> getBackgroundList() {
        return backgroundList;
    }

    public void setBackgroundList(List<String> backgroundList) {
        this.backgroundList = backgroundList;
    }

    public List<QuestionAnswer> getQuestionAnswerList() {
        return questionAnswerList;
    }

    public void setQuestionAnswerList(List<QuestionAnswer> questionAnswerList) {
        this.questionAnswerList = questionAnswerList;
    }

    public int getDiscussStatus() {
        return discussStatus;
    }

    public void setDiscussStatus(int discussStatus) {
        this.discussStatus = discussStatus;
    }

    public int getSegmentSummaryCursor() {
        return segmentSummaryCursor;
    }

    public void setSegmentSummaryCursor(int segmentSummaryCursor) {
        this.segmentSummaryCursor = segmentSummaryCursor;
    }

    public int getSegmentQuestionCursor() {
        return segmentQuestionCursor;
    }

    public void setSegmentQuestionCursor(int segmentQuestionCursor) {
        this.segmentQuestionCursor = segmentQuestionCursor;
    }

    public int getSegmentUnderstandCursor() {
        return segmentUnderstandCursor;
    }

    public void setSegmentUnderstandCursor(int segmentUnderstandCursor) {
        this.segmentUnderstandCursor = segmentUnderstandCursor;
    }

    public int getTimeSlicedSummaryCursor() {
        return timeSlicedSummaryCursor;
    }

    public void setTimeSlicedSummaryCursor(int timeSlicedSummaryCursor) {
        this.timeSlicedSummaryCursor = timeSlicedSummaryCursor;
    }

    public int getKeyWordCursor() {
        return keyWordCursor;
    }

    public void setKeyWordCursor(int keyWordCursor) {
        this.keyWordCursor = keyWordCursor;
    }

    public int getKeySentenceCursor() {
        return keySentenceCursor;
    }

    public void setKeySentenceCursor(int keySentenceCursor) {
        this.keySentenceCursor = keySentenceCursor;
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public void setSentenceList(List<Sentence> sentenceList) {
        this.sentenceList = sentenceList;
    }

    public int getSentenceSummaryCursor() {
        return sentenceSummaryCursor;
    }

    public void setSentenceSummaryCursor(int sentenceSummaryCursor) {
        this.sentenceSummaryCursor = sentenceSummaryCursor;
    }

    public int getSegmentCorrectCursor() {
        return segmentCorrectCursor;
    }

    public void setSegmentCorrectCursor(int segmentCorrectCursor) {
        this.segmentCorrectCursor = segmentCorrectCursor;
    }

    public List<String> getSegmentRemarkList() {
        return segmentRemarkList;
    }

    public void setSegmentRemarkList(List<String> segmentRemarkList) {
        this.segmentRemarkList = segmentRemarkList;
    }

    public int getSegmentRemarkCursor() {
        return segmentRemarkCursor;
    }

    public void setSegmentRemarkCursor(int segmentRemarkCursor) {
        this.segmentRemarkCursor = segmentRemarkCursor;
    }

    public List<String> getSegmentRestateList() {
        return segmentRestateList;
    }

    public void setSegmentRestateList(List<String> segmentRestateList) {
        this.segmentRestateList = segmentRestateList;
    }

    public int getSegmentRestateCursor() {
        return segmentRestateCursor;
    }

    public void setSegmentRestateCursor(int segmentRestateCursor) {
        this.segmentRestateCursor = segmentRestateCursor;
    }

    public List<String> getSegmentAnalogyList() {
        return segmentAnalogyList;
    }

    public void setSegmentAnalogyList(List<String> segmentAnalogyList) {
        this.segmentAnalogyList = segmentAnalogyList;
    }

    public int getSegmentAnalogyCursor() {
        return segmentAnalogyCursor;
    }

    public void setSegmentAnalogyCursor(int segmentAnalogyCursor) {
        this.segmentAnalogyCursor = segmentAnalogyCursor;
    }

    public List<String> getSegmentContinueList() {
        return segmentContinueList;
    }

    public void setSegmentContinueList(List<String> segmentContinueList) {
        this.segmentContinueList = segmentContinueList;
    }

    public int getSegmentContinueCursor() {
        return segmentContinueCursor;
    }

    public void setSegmentContinueCursor(int segmentContinueCursor) {
        this.segmentContinueCursor = segmentContinueCursor;
    }

    public List<String> getSegmentMultiangleList() {
        return segmentMultiangleList;
    }

    public void setSegmentMultiangleList(List<String> segmentMultiangleList) {
        this.segmentMultiangleList = segmentMultiangleList;
    }

    public int getSegmentMultiangleCursor() {
        return segmentMultiangleCursor;
    }

    public void setSegmentMultiangleCursor(int segmentMultiangleCursor) {
        this.segmentMultiangleCursor = segmentMultiangleCursor;
    }

    public List<String> getSegmentLogicList() {
        return segmentLogicList;
    }

    public void setSegmentLogicList(List<String> segmentLogicList) {
        this.segmentLogicList = segmentLogicList;
    }

    public int getSegmentLogicCursor() {
        return segmentLogicCursor;
    }

    public void setSegmentLogicCursor(int segmentLogicCursor) {
        this.segmentLogicCursor = segmentLogicCursor;
    }
}
