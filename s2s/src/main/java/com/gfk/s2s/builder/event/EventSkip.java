package com.gfk.s2s.builder.event;


import com.gfk.s2s.builder.eventInterface.IEventSkip;


public class EventSkip extends EventBase implements IEventSkip {

    public EventSkip(String mediaId, long streamPosition, String presentationId, int segmentNumber, int requestNumber, int segmentDuration, long usageTime) {
        super(mediaId, streamPosition,presentationId, requestNumber, segmentNumber, segmentDuration, usageTime);
    }
}
