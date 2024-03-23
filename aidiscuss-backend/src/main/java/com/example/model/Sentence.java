package com.example.model;

public class Sentence {
    private String text;
    private String beginTime;

    public Sentence(String text, String beginTime) {
        this.text = text;
        this.beginTime = beginTime;
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

}
