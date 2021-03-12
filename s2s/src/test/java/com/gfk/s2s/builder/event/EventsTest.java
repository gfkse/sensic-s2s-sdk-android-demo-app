package com.gfk.s2s.builder.event;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibTestRunner.class)
public class EventsTest {

    private String media;
    private String contentId;
    private int streamPosition;
    private String presentationId;
    private int segmentStateItemNumber;
    private int segmentNumber;
    private int segmentDuration;
    private String screen;
    private String volume;
    private HashMap<String, String> options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "mute"); put("deviceType", "TV");}};
    private IEventPlayOptions playOptions = new IEventPlayOptions() {
        @Override
        public String getVolume() {
            return options.get("volume");
        }

        @Override
        public String getScreen() {
            return options.get("screen");
        }

        @Override
        public String getDeviceType() { return options.get("deviceType"); }
    };
    private Map customParameters;
    private String streamStartTime = "";
    private int streamOffset = 20;
    private long usageTime = 0L;

    @Before
    public void setUp() {
        media = "myMediaId";
        contentId = "myContentId";
        streamPosition = 0;
        presentationId = "1234";
        segmentStateItemNumber = 1;
        segmentNumber = 1;
        segmentDuration = 120;
        screen = "fullscreen";
        volume = "mute";
        customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "qwe");}};
    }

    @Test
    public void testEventImpression() {
        EventImpression eventImpression = new EventImpression(media, contentId, customParameters);

        assertThat(eventImpression.getMediaId()).isEqualTo(media);
        assertThat(eventImpression.getContentId()).isEqualTo(contentId);
        assertThat(eventImpression.getCustomParams().containsKey("cp1")).isTrue();
        assertThat(eventImpression.getCustomParams().containsKey("cp2")).isTrue();
        assertThat(eventImpression.getCustomParams().containsKey("cp3")).isFalse();
    }

    @Test
    public void testEventPlay() {
        EventPlay eventPlay = new EventPlay(media, contentId, streamPosition, streamStartTime, streamOffset, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.ondemand, playOptions, customParameters);

        assertThat(eventPlay.getMediaId()).isEqualTo(media);
        assertThat(eventPlay.getContentId()).isEqualTo(contentId);
        assertThat(eventPlay.getStreamPosition()).isEqualTo(streamPosition);
        assertThat(eventPlay.getPresentationId()).isEqualTo(presentationId);
        assertThat(eventPlay.getSegmentStateItemNumber()).isEqualTo(segmentStateItemNumber);
        assertThat(eventPlay.getSegmentNumber()).isEqualTo(segmentNumber);
        assertThat(eventPlay.getSegmentDuration()).isEqualTo(segmentDuration);
        assertThat(eventPlay.getOptions()).isEqualTo(playOptions);
        assertThat(eventPlay.getCustomParams().containsKey("cp1")).isTrue();
    }

    @Test
    public void testEventPlayOptionalProperties() {
        EventPlay eventPlay = new EventPlay(media, contentId, streamPosition, streamStartTime, streamOffset, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.ondemand, playOptions, customParameters);

        assertThat(eventPlay.getMediaId()).isEqualTo(media);
        assertThat(eventPlay.getContentId()).isEqualTo(contentId);
        assertThat(eventPlay.getStreamPosition()).isEqualTo(streamPosition);
        assertThat(eventPlay.getPresentationId()).isEqualTo(presentationId);
        assertThat(eventPlay.getSegmentStateItemNumber()).isEqualTo(segmentStateItemNumber);
        assertThat(eventPlay.getSegmentNumber()).isEqualTo(segmentNumber);
        assertThat(eventPlay.getSegmentDuration()).isEqualTo(segmentDuration);
        assertThat(eventPlay.getOptions()).isEqualTo(playOptions);
        assertThat(eventPlay.getCustomParams().containsKey("cp1")).isTrue();
        assertThat(eventPlay.getCustomParams().containsKey("cp2")).isTrue();
        assertThat(eventPlay.getCustomParams().containsKey("cp3")).isFalse();
    }

    @Test
    public void testEventStop() {
        EventStop eventStop = new EventStop(media, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, usageTime);

        assertThat(eventStop.getMediaId()).isEqualTo(media);
        assertThat(eventStop.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testEventSkip() {
        EventSkip eventSkip = new EventSkip(media, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, usageTime);

        assertThat(eventSkip.getMediaId()).isEqualTo(media);
        assertThat(eventSkip.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testEventScreen() {
        EventScreen eventScreen = new EventScreen(media, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, screen, usageTime);

        assertThat(eventScreen.getMediaId()).isEqualTo(media);
        assertThat(eventScreen.getStreamPosition()).isEqualTo(streamPosition);
        assertThat(eventScreen.getScreen()).isEqualTo(screen);
    }

    @Test
    public void testEventVolume() {
        EventVolume eventVolume = new EventVolume(media, streamPosition, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, volume, usageTime);

        assertThat(eventVolume.getMediaId()).isEqualTo(media);
        assertThat(eventVolume.getStreamPosition()).isEqualTo(streamPosition);
        assertThat(eventVolume.getVolume()).isEqualTo(volume);
    }

    @Test
    public void test_empty_content_start_time() {
        EventPlay eventPlay = new EventPlay(media, contentId, streamPosition, "", 0, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        assertThat(eventPlay.getStreamStartTime()).isEqualTo(usageTime);
    }

    @Test
    public void test_streamposition_live_with_offset() {
        EventPlay eventPlay = new EventPlay(media, contentId, streamPosition, "", streamOffset, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        assertThat(eventPlay.getStreamStartTime()).isEqualTo(usageTime - streamOffset);
    }

    @Test
    public void test_vod_content_time() {
        EventPlay eventPlay = new EventPlay(media, contentId, streamPosition, "", 0, usageTime, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.ondemand, playOptions, customParameters);
        assertThat(eventPlay.getStreamStartTime()).isEqualTo(usageTime);
    }
}
