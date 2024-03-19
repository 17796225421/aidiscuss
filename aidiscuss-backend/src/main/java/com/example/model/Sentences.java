package com.example.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sentences {
    private BlockingQueue<Sentence> queue = new LinkedBlockingQueue<>();

    // 将句子插入队列末尾
    public void addSentence(Sentence sentence) {
        queue.add(sentence);
    }

    // 从队列头部弹出句子,如果队列为空则阻塞
    public Sentence popSentence() throws InterruptedException {
        return queue.take();
    }
}
