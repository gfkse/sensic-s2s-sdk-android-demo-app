package com.gfk.s2s.builder.buffer;

import java.util.Map;

public class BufferImpression extends BufferCommon {
    private String contentId;
    private Map customParameters;

    public BufferImpression(String contentId, Map customParameters) {
        this.contentId = contentId;
        this.customParameters = customParameters;
    }

    public String getContentId() {
        return contentId;
    }

    public Map getCustomParams() {
        return customParameters;
    }
}
