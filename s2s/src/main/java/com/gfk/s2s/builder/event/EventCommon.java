package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventCommon;

public abstract class EventCommon implements IEventCommon {
    private String mediaId;

    EventCommon(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }
}
