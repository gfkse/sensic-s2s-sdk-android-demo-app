package com.gfk.s2s.builder.buffer;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.s2sagent.StreamPositionCallback;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class BufferTest {
    private String contentId;
    private String streamId;
    private int streamOffset;
    private String screen;
    private String volume;
    private HashMap<String, String> customParameters;
    private HashMap<String, String> options;
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
        public String getDeviceType() { return  options.get("deviceType"); }
    };
    private int streamPosition;
    private String streamStartTime = "";
    private long usageTime = 0L;

    private final StreamPositionCallback streamPositionCallback = new StreamPositionCallback() {
        @Override
        public Integer onCallback() {
            return streamPosition;
        }
    };

    @Before
    public void setUp() throws Exception {
        contentId = "myContentId";
        streamId = "F1 - Sport";
        streamOffset = 23;
        screen = "fullscreen";
        volume = "50%";
        options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "100"); put("deviceType", "TV");}};
        customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "qwe");}};
        streamPosition = 42;
    }

    @Test
    public void testCreatePlay() {
        BufferPlay bufferPlay = new BufferPlay(contentId, streamOffset, streamId, playOptions, customParameters, streamPosition, streamStartTime, usageTime, AllowedPlayType.live);

        assertThat(bufferPlay.getContentId()).isEqualTo(contentId);
        assertThat(bufferPlay.getStreamOffset()).isEqualTo(streamOffset);
        assertThat(bufferPlay.getStreamId()).isEqualTo(streamId);
        assertThat(bufferPlay.getOptions()).isEqualTo(playOptions);
        assertThat(bufferPlay.getCustomParams()).isEqualTo(customParameters);
        assertThat(bufferPlay.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testCreateStop() {
        BufferStop bufferStop = new BufferStop(streamPosition, usageTime);

        assertThat(bufferStop.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testCreateSkip() {
        BufferSkip bufferSkip = new BufferSkip(streamPosition, usageTime);

        assertThat(bufferSkip.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testCreateScreen() {
        BufferScreen bufferScreen = new BufferScreen(screen, streamPosition, usageTime);

        assertThat(bufferScreen.getScreen()).isEqualTo(screen);
        assertThat(bufferScreen.getStreamPosition()).isEqualTo(streamPosition);
    }

    @Test
    public void testCreateVolume() {
        BufferVolume bufferVolume = new BufferVolume(volume, streamPosition, usageTime);

        assertThat(bufferVolume.getVolume()).isEqualTo(volume);
        assertThat(bufferVolume.getStreamPosition()).isEqualTo(streamPosition);
    }
}