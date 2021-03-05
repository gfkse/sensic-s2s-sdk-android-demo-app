package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.eventInterface.IEventPlay;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EventPlay extends EventBase implements IEventPlay {
    private long streamStartTime;
    private String contentId;
    private Map customParameters;
    private IEventPlayOptions options;

    public EventPlay(String mediaId, String contentId, long streamPosition, String streamStartTime, int streamOffset, long usageTime, String presentationId, int segmentNumber, int segmentStateItemNumber, int segmentDuration, AllowedPlayType playType, IEventPlayOptions options, Map customParameters) {
        super(mediaId, playType == AllowedPlayType.ondemand ? streamPosition : -1, presentationId, segmentStateItemNumber, segmentNumber, segmentDuration, usageTime);
        this.contentId = contentId;
        this.options = options;
        this.customParameters = customParameters;
        this.streamStartTime = streamStartTime.length() == 0 ? usageTime - streamOffset : parseDate(streamStartTime) - streamOffset;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public IEventPlayOptions getOptions() {
        return options;
    }

    @Override
    public Map getCustomParams() {
        return customParameters;
    }

    public long getStreamStartTime() {
        return streamStartTime;
    }

    private long parseDate(String value) {
        SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", new Locale("de", "DE"));
        dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return dateFormatUtc.parse(value).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }


}
