package com.example.model;

public class SimpleSketch {
    private final int intId;
    private final long[] bits;

    public SimpleSketch(int intId, long[] bits) {
        this.intId = intId;
        this.bits = bits;
    }

    public int getIntId() {
        return intId;
    }

    public long[] getBits() {
        return bits;
    }
}

