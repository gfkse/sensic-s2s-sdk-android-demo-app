package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.eventInterface.IEventImpression;

import java.util.Map;

public class EventImpression extends EventCommon implements IEventImpression{
    private String contentId;
    private Map customParameters;


    public EventImpression(String mediaId, String contentId, Map customParameters) {
        super(mediaId);
        this.contentId = contentId;
        this.customParameters = customParameters;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public Map getCustomParams() {
        return customParameters;
    }
}
