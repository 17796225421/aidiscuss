package com.example.model;

public class Sentence {
    private int index;
    private String text;
    private double beginTime;
    private double totalTime;

    public Sentence(int index, String text, double beginTime, double totalTime) {
        this.index = index;
        this.text = text;
        this.beginTime = beginTime;
        this.totalTime = totalTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(double beginTime) {
        this.beginTime = beginTime;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
}
