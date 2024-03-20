package com.example.model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Sentences {
    // 使用ConcurrentLinkedQueue来保证线程安全
    private ConcurrentLinkedQueue<Sentence> queue = new ConcurrentLinkedQueue<>();

    // 将句子插入队列末尾
    public void addSentence(Sentence sentence) {
        queue.add(sentence);
    }

    // 从队列头部弹出句子，如果队列为空则立即返回null
    public Sentence popSentence() {
        return queue.poll();
    }
}