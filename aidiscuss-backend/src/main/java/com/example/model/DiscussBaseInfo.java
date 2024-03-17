package com.example.model;

public class DiscussBaseInfo {
    String discussId;
    String discussName;

    public DiscussBaseInfo(String discussId, String discussName) {
        this.discussId = discussId;
        this.discussName = discussName;
    }

    public String getDiscussName() {
        return discussName;
    }

    public void setDiscussName(String discussName) {
        this.discussName = discussName;
    }
    public String getDiscussId() {
        return discussId;
    }

    public void setDiscussId(String discussId) {
        this.discussId = discussId;
    }
}
