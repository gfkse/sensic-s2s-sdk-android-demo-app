package com.gfk.s2s.builder.segment;

public class Segment implements ISegment {

    private long streamPosition;
    private String presentationId;
    private int segmentNumber;
    private int stateItemNumber;
    private int segmentDuration;

    public Segment(int stateItemNumber, int segmentNumber, long streamPosition, String presentationId, int segmentDuration) {
        this.stateItemNumber = stateItemNumber;
        this.segmentNumber = segmentNumber;
        this.streamPosition = streamPosition;
        this.presentationId = presentationId;
        this.segmentDuration = segmentDuration;
    }

    public long getStreamPosition() {
        return streamPosition;
    }

    public String getPresentationId() {
        return presentationId;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public int getStateItemNumber() {
        return stateItemNumber;
    }

    public int getSegmentDuration() {
        return segmentDuration;
    }
}
