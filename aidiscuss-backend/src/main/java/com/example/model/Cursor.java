package com.example.model;

public class Cursor {
    int externCursor;
    int wireCursor;
    int virtualCursor;

    public int getExternCursor() {
        return externCursor;
    }

    public void setExternCursor(int externCursor) {
        this.externCursor = externCursor;
    }

    public int getWireCursor() {
        return wireCursor;
    }

    public void setWireCursor(int wireCursor) {
        this.wireCursor = wireCursor;
    }

    public int getVirtualCursor() {
        return virtualCursor;
    }

    public void setVirtualCursor(int virtualCursor) {
        this.virtualCursor = virtualCursor;
    }
}
