package com.gfk.s2s.builder.buffer;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;

import java.util.Map;

public class BufferPlay extends BufferBase {
    private AllowedPlayType playType;
    private String streamStartTime;
    private String contentId;
    private String streamId;
    private IEventPlayOptions options;
    private Map customParameters;
    private int streamOffset;

    public BufferPlay(String contentId, int streamOffset, String streamId, IEventPlayOptions options, Map customParameters, long streamPosition, String streamStartTime, long usageTime, AllowedPlayType playType) {
        super(streamPosition, usageTime);
        this.contentId = contentId;
        this.streamOffset = streamOffset;
        this.streamId = streamId;
        this.options = options;
        this.customParameters = customParameters;
        this.streamStartTime = streamStartTime;
        this.playType = playType;
    }

    public String getContentId() {
        return contentId;
    }

    public IEventPlayOptions getOptions() {
        return options;
    }

    public Map getCustomParams() {
        return customParameters;
    }

    public String getStreamId() {
        return streamId;
    }

    public int getStreamOffset() {
        return streamOffset;
    }

    public String getStreamStartTime() { return streamStartTime; }

    public AllowedPlayType getPlayType() {
        return playType;
    }
}
