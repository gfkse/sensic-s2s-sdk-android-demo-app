package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventScreen;


public class EventScreen extends EventBase implements IEventScreen {

    private String screen;

    public EventScreen(String mediaId, long streamPosition, String presentationId, int segmentNumber, int requestNumber, int segmentDuration, String screen, long usageTime) {
        super(mediaId, streamPosition, presentationId, requestNumber, segmentNumber, segmentDuration, usageTime);
        this.screen = screen;
    }


    @Override
    public String getScreen() {
        return screen;
    }
}
