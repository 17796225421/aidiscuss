package com.example.model;

public enum MicTypeEnum {
    EXTERN("M3"),
    WIRE("Realtek"),
    VIRTUAL("B1");

    private final String value;

    MicTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}