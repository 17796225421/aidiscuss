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
    int externCursor;
    int wireCursor;
    int virtualCursor;
    private List<String> segmentSummaryList;

    public DiscussInfo() {
        startTimeList = new ArrayList<>();
        stopTimeList = new ArrayList<>();
        segmentSummaryList=new ArrayList<>();
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

    public int getExternCursor() {
        return externCursor;
    }

    public void setExternCursor(int externCursor) {
        this.externCursor = externCursor;
    }

    public int getWireCursor() {
        return wireCursor;
    }

    public void setWireCursor(int wireCursor) {
        this.wireCursor = wireCursor;
    }

    public int getVirtualCursor() {
        return virtualCursor;
    }

    public void setVirtualCursor(int virtualCursor) {
        this.virtualCursor = virtualCursor;
    }

    public List<String> getSegmentSummaryList() {
        return segmentSummaryList;
    }

    public void setSegmentSummaryList(List<String> segmentSummaryList) {
        this.segmentSummaryList = segmentSummaryList;
    }
}
