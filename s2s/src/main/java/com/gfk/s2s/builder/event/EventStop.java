package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventStop;

public class EventStop extends EventBase implements IEventStop {

    public EventStop(String mediaId, long streamPosition, String presentationId, int segmentNumber, int segmentStateItemNumber, int segmentDuration, long usageTime) {
        super(mediaId, streamPosition,presentationId, segmentStateItemNumber, segmentNumber, segmentDuration, usageTime);
    }
}
