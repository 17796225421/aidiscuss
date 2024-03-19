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
}
