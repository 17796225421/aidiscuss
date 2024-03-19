package com.example.model;

public class MicSentences {
    private Sentences externMicSentences;
    private Sentences wireMicSentences;
    private Sentences virtualMicSentences;

    public Sentences getExternMicSentences() {
        return externMicSentences;
    }

    public void setExternMicSentences(Sentences externMicSentences) {
        this.externMicSentences = externMicSentences;
    }

    public Sentences getWireMicSentences() {
        return wireMicSentences;
    }

    public void setWireMicSentences(Sentences wireMicSentences) {
        this.wireMicSentences = wireMicSentences;
    }

    public Sentences getVirtualMicSentences() {
        return virtualMicSentences;
    }

    public void setVirtualMicSentences(Sentences virtualMicSentences) {
        this.virtualMicSentences = virtualMicSentences;
    }
}
