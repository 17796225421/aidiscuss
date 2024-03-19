package com.example.model;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;

import javax.sound.sampled.TargetDataLine;

public class MicAndTranscriber {
    private TargetDataLine targetDataLine;
    private SpeechTranscriber transcriber;
    private Sentences sentences = new Sentences();

    public MicAndTranscriber() {
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
}