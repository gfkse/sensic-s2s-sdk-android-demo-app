package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventBase;

public abstract class EventBase extends EventCommon implements IEventBase {

    private long usageTime;
    private int streamPosition;
    private String presentationId;
    private int segmentStateItemNumber;
    private int segmentNumber;
    private int segmentDuration;

    EventBase(String mediaId, long streamPosition, String presentationId, int segmentStateItemNumber, int segmentNumber, int segmentDuration, long usageTime) {
        super(mediaId);

        this.streamPosition = (int) streamPosition;
        this.presentationId = presentationId;
        this.segmentStateItemNumber = segmentStateItemNumber;
        this.segmentNumber = segmentNumber;
        this.segmentDuration = segmentDuration;
        this.usageTime = usageTime;
    }

    @Override
    public int getStreamPosition() {
        return streamPosition;
    }

    @Override
    public String getPresentationId() {
        return presentationId;
    }

    public int getSegmentStateItemNumber() {
        return segmentStateItemNumber;
    }

    @Override
    public int getSegmentNumber() {
        return segmentNumber;
    }

    @Override
    public int getSegmentDuration() {
        return segmentDuration;
    }

    @Override
    public long getUsageTime() {
        return usageTime;
    }
}
