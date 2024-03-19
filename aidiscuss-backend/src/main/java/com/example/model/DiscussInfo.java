package com.example.model;

public class DiscussInfo {
    String discussId;
    String discussName;
    MicSwitchInfo micSwitchInfo;

    MicSentences micSentences;

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

    public MicSwitchInfo getMicSwitchInfo() {
        return micSwitchInfo;
    }

    public void setMicSwitchInfo(MicSwitchInfo micSwitchInfo) {
        this.micSwitchInfo = micSwitchInfo;
    }

    public MicSentences getMicSentences() {
        return micSentences;
    }

    public void setMicSentences(MicSentences micSentences) {
        this.micSentences = micSentences;
    }
}
