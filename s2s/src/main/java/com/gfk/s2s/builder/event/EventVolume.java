package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventVolume;

/**
 * Created by bodo.hinueber on 06.09.2017.
 */

public class EventVolume extends EventBase implements IEventVolume {

    private String volume;

    public EventVolume(String mediaId, long streamPosition, String presentationId, int segmentNumber, int segmentStateItemNumber, int segmentDuration, String volume, long usageTime) {
        super(mediaId, streamPosition,presentationId, segmentStateItemNumber, segmentNumber, segmentDuration, usageTime);
        this.volume = volume;
    }

    @Override
    public String getVolume() {
        return volume;
    }
}
