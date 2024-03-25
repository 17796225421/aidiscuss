package com.example.model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Sentencestmp {
    // 使用ConcurrentLinkedQueue来保证线程安全
    private ConcurrentLinkedQueue<Sentencetmp> queue = new ConcurrentLinkedQueue<>();

    // 将句子插入队列末尾
    public void addSentence(Sentencetmp sentencetmp) {
        queue.add(sentencetmp);
    }

    // 从队列头部弹出句子，如果队列为空则立即返回null
    public Sentencetmp popSentence() {
        return queue.poll();
    }

    public ConcurrentLinkedQueue<Sentencetmp> getQueue() {
        return queue;
    }

    public void setQueue(ConcurrentLinkedQueue<Sentencetmp> queue) {
        this.queue = queue;
    }

}