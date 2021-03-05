package com.gfk.s2s.builder;

import com.gfk.s2s.builder.event.EventCommon;
import com.gfk.s2s.builder.event.EventImpression;
import com.gfk.s2s.builder.event.EventPlay;
import com.gfk.s2s.builder.event.EventScreen;
import com.gfk.s2s.builder.event.EventSkip;
import com.gfk.s2s.builder.event.EventStop;
import com.gfk.s2s.builder.event.EventVolume;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.segment.ISegment;

import java.util.Map;

public class EventBuilder {
    private String mediaId;

    public EventBuilder(String mediaId) {
        this.mediaId = mediaId;
    }

    public EventImpression buildEventImpression(String contentId, Map customParams) {
        return new EventImpression(mediaId, contentId, customParams);
    }

    public EventPlay buildEventPlay(ISegment segment, String streamStartTime, int streamOffset, long usageTime, String contentId, AllowedPlayType playType, IEventPlayOptions options, Map customParameters) {
        return new EventPlay(mediaId, contentId, segment.getStreamPosition(), streamStartTime, streamOffset, usageTime, segment.getPresentationId(), segment.getSegmentNumber(), segment.getStateItemNumber(), segment.getSegmentDuration(), playType, options, customParameters);
    }

    public EventStop buildEventStop(ISegment segment, long usageTime) {
        return new EventStop(this.mediaId, segment.getStreamPosition(), segment.getPresentationId(), segment.getSegmentNumber(), segment.getStateItemNumber(), segment.getSegmentDuration(), usageTime);
    }

    public EventSkip buildEventSkip(ISegment segment, long usageTime) {
        return new EventSkip(this.mediaId, segment.getStreamPosition(), segment.getPresentationId(), segment.getSegmentNumber(), segment.getStateItemNumber(), segment.getSegmentDuration(), usageTime);
    }

    public EventVolume buildEventVolume(ISegment segment, String volume, long usageTime) {
        return new EventVolume(mediaId,segment.getStreamPosition(), segment.getPresentationId(), segment.getSegmentNumber(), segment.getStateItemNumber(), segment.getSegmentDuration(), volume, usageTime);
    }

    public EventScreen buildEventScreen(ISegment segment, String screen, long usageTime) {
        return new EventScreen(mediaId,segment.getStreamPosition(), segment.getPresentationId(), segment.getSegmentNumber(), segment.getStateItemNumber(), segment.getSegmentDuration(), screen, usageTime);
    }
}
