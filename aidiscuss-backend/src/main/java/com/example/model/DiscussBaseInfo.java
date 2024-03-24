package com.example.model;

public class DiscussBaseInfo {
    String discussId;
    String discussName;

    int discussStatus;

    public DiscussBaseInfo(String discussId, String discussName, int discussStatus) {
        this.discussId = discussId;
        this.discussName = discussName;
        this.discussStatus = discussStatus;
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

    public int getDiscussStatus() {
        return discussStatus;
    }

    public void setDiscussStatus(int discussStatus) {
        this.discussStatus = discussStatus;
    }
}
