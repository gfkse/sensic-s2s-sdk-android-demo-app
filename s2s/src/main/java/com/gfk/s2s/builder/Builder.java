package com.gfk.s2s.builder;

import com.gfk.s2s.collector.ICollector;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;

public class Builder {

    private EventBuilder eventBuilder;
    private RequestBuilder requestBuilder;
    private SegmentBuilder segmentBuilder;

    public Builder(String mediaId, ICollector collector) {
        eventBuilder = new EventBuilder(mediaId);
        segmentBuilder = new SegmentBuilder(collector.getSegmentConfig());
        requestBuilder = new RequestBuilder(collector);
    }

    public void setStreamPositionCallback(IStreamPositionCallback streamPositionCallback) {
        segmentBuilder.setStreamPositionCallback(streamPositionCallback);
    }

    public EventBuilder getEventBuilder() {
        return eventBuilder;
    }

    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public SegmentBuilder getSegmentBuilder() {
        return segmentBuilder;
    }
}
