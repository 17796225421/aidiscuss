package com.example.model;

import java.util.ArrayList;
import java.util.List;

public class DiscussInfo {
    String discussId;
    String discussName;
    MicSentences micSentences;
    private List<String> startTimeList;
    private List<String> stopTimeList;

    public DiscussInfo() {
        startTimeList = new ArrayList<>();
        stopTimeList = new ArrayList<>();
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
}
