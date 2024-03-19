package com.example.model;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;

import javax.sound.sampled.TargetDataLine;

public class MicAndTranscriber {
    private TargetDataLine targetDataLine;
    private SpeechTranscriber transcriber;
    private Sentences sentences;

    public MicAndTranscriber(TargetDataLine targetDataLine, SpeechTranscriber transcriber, Sentences sentences) {
        this.targetDataLine = targetDataLine;
        this.transcriber = transcriber;
        this.sentences = sentences;
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