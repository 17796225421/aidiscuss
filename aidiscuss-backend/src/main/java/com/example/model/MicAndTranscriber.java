package com.example.model;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;

import javax.sound.sampled.TargetDataLine;

public class MicAndTranscriber {
    volatile boolean running = true;
    private TargetDataLine targetDataLine;
    private SpeechTranscriber transcriber;
    private Sentences sentences;
    private Thread audioThread; // 新增音频线程字段

    public MicAndTranscriber(TargetDataLine targetDataLine, SpeechTranscriber transcriber, Sentences sentences) {
        this.targetDataLine = targetDataLine;
        this.transcriber = transcriber;
        this.sentences = sentences;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public TargetDataLine getTargetDataLine() {
        return targetDataLine;
    }

    public void setTargetDataLine(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }

    public SpeechTranscriber getTranscriber() {
        return transcriber;
    }

    public void setTranscriber(SpeechTranscriber transcriber) {
        this.transcriber = transcriber;
    }

    public Sentences getSentences() {
        return sentences;
    }

    public void setSentences(Sentences sentences) {
        this.sentences = sentences;
    }

    // 新增获取和设置音频线程的方法
    public Thread getAudioThread() {
        return audioThread;
    }

    public void setAudioThread(Thread audioThread) {
        this.audioThread = audioThread;
    }
}