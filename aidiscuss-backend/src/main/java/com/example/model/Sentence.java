package com.example.model;

public class Sentence {
    private String text;
    private String beginTime;
    private String summary;

    public Sentence(String text, String beginTime) {
        this.text = text;
        this.beginTime = beginTime;
        this.summary = "";
    }

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
