package com.example.model;

public class MicSentencestmp {
    private Sentencestmp externMicSentencestmp;
    private Sentencestmp wireMicSentencestmp;
    private Sentencestmp virtualMicSentencestmp;

    public Sentencestmp getExternMicSentences() {
        return externMicSentencestmp;
    }

    public void setExternMicSentences(Sentencestmp externMicSentencestmp) {
        this.externMicSentencestmp = externMicSentencestmp;
    }

    public Sentencestmp getWireMicSentences() {
        return wireMicSentencestmp;
    }

    public void setWireMicSentences(Sentencestmp wireMicSentencestmp) {
        this.wireMicSentencestmp = wireMicSentencestmp;
    }

    public Sentencestmp getVirtualMicSentences() {
        return virtualMicSentencestmp;
    }

    public void setVirtualMicSentences(Sentencestmp virtualMicSentencestmp) {
        this.virtualMicSentencestmp = virtualMicSentencestmp;
    }
}
