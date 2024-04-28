package com.example.model;

public class Sentence {
    String text;
    String beginTime;
    MicTypeEnum micTypeEnum;
    String summary;
    int score;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public MicTypeEnum getMicTypeEnum() {
        return micTypeEnum;
    }

    public void setMicTypeEnum(MicTypeEnum micTypeEnum) {
        this.micTypeEnum = micTypeEnum;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
