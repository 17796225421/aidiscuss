package com.example.model;

public enum DiscussStatusEnum {
    CREATED(0),
    STARTED(1),
    STOPED(2),
    CLOSED(3);

    private final int value;

    DiscussStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
