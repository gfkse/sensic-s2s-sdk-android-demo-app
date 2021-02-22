package com.gfk.s2s.builder;

import android.content.Context;

import com.gfk.s2s.builder.event.EventImpression;
import com.gfk.s2s.builder.event.EventPlay;
import com.gfk.s2s.builder.event.EventScreen;
import com.gfk.s2s.builder.event.EventSkip;
import com.gfk.s2s.builder.event.EventStop;
import com.gfk.s2s.builder.event.EventVolume;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.builder.request.RequestImpression;
import com.gfk.s2s.builder.request.RequestPlay;
import com.gfk.s2s.builder.request.RequestScreen;
import com.gfk.s2s.builder.request.RequestStop;
import com.gfk.s2s.builder.request.RequestVolume;
import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.ICollector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibTestRunner.class)
public class RequestBuilderTest {

    private String mediaId;
    private String contentId;
    private int streamPosition;
    private String presentationId;
    private int segmentStateItemNumber;
    private int segmentNumber;
    private int segmentDuration;
    private String screen;
    private String volume;
    private HashMap<String, String> options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "mute"); put("deviceType", "TV");}};
    private HashMap customParameters;
    private String streamStartTime = "";
    private int streamOffset = 20;
    private long usageTime = 0L;

    RequestBuilder requestBuilder;
    ICollector collector;

    private Context context;


    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        mediaId = "mediaId";
        contentId = "contentId";
        streamPosition = 0;
        presentationId = "1234";
        segmentStateItemNumber = 1;
        segmentNumber = 1;
        segmentDuration = 120;
        screen = "fullscreen";
        volume = "50%";
        customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "cba");}};

        collector = new Collector(context);
        requestBuilder = new RequestBuilder(collector);

    }

    @Test
    public void testIfEventImpressionObjectCanBeMappedToRequestImpression() {
        EventImpression event = new EventImpression(mediaId, contentId, customParameters);
        IRequest request = requestBuilder.buildRequestImpression(event);
        assertThat(request instanceof RequestImpression).isTrue();
    }

    @Test
    public void testIfEventPlayObjectCanBeMappedToRequestPlay() {
        IEventPlayOptions playOptions = new IEventPlayOptions() {
            @Override
            public String getVolume() {
                return options.get("volume");
            }

            @Override
            public String getScreen() {
                return options.get("screen");
            }

            @Override
            public String getDeviceType() {
                return options.get("deviceType");
            }
        };
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, streamStartTime, streamOffset, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = requestBuilder.buildRequestPlay(event);
        assertThat(request instanceof RequestPlay).isTrue();
    }

    @Test
    public void testIfEventStopObjectCanBeMappedToRequestStop() {
        EventStop event = new EventStop(mediaId, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, usageTime);
        IRequest request = requestBuilder.buildRequestStop(event);
        assertThat(request instanceof RequestStop).isTrue();
    }

    @Test
    public void testIfEventSkipObjectCanBeMappedToRequestSkip() {
        EventSkip event = new EventSkip(mediaId, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, usageTime);
        IRequest request = requestBuilder.buildRequestSkip(event);
        assertThat(request instanceof RequestStop).isTrue();
    }

    @Test
    public void testIfEventVolumeObjectCanBeMappedToRequestPlay() {
        EventVolume event = new EventVolume(mediaId, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, volume, usageTime);
        IRequest request = requestBuilder.buildRequestVolume(event);
        assertThat(request instanceof RequestVolume).isTrue();
    }

    @Test
    public void testIfEventScreenObjectCanBeMappedToRequestScreen() {
        EventScreen event = new EventScreen(mediaId, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, screen, usageTime);
        IRequest request = requestBuilder.buildRequestScreen(event);
        assertThat(request instanceof RequestScreen).isTrue();
    }
}