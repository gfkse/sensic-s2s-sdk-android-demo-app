package com.gfk.s2s.collector;

public class SegmentConfig {
    private int minSegmentDuration;
    private int maxStateItemsNumber;
    private int maxSegmentNumber;

    public SegmentConfig(int minSegmentDuration, int maxStateItemsNumber, int maxSegmentNumber) {
        this.minSegmentDuration = minSegmentDuration;
        this.maxStateItemsNumber = maxStateItemsNumber;
        this.maxSegmentNumber = maxSegmentNumber;
    }

    public int getMinSegmentDuration() {
        return minSegmentDuration;
    }

    public int getMaxStateItemsNumber() {
        return maxStateItemsNumber;
    }

    public int getMaxSegmentNumber() {
        return maxSegmentNumber;
    }
}
