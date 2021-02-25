package com.gfk.s2s.s2sagent;

import android.content.Context;

import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.processor.Processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class S2SAgentTest {

    private S2SAgent s2SAgent;
    private Context context;

    private String contentId;
    private int streamPosition;
    private String streamId;
    private String screen;
    private String volume;
    private Map customParams;
    private StreamPositionCallback streamPositionCallback;

    //Will be deprecated soon
    private VideoPositionCallback videoPositionCallback;

    final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";

    @Before
    public void setUp() {
        streamPositionCallback = () -> streamPosition;
        videoPositionCallback = () -> streamPosition;

        context = RuntimeEnvironment.application;
        s2SAgent = new S2SAgent(unitTestConfigUrl, "123", streamPositionCallback, context);

        contentId = "123";
        streamPosition = 2;
        streamId = "Bundesliga bla bla";
        screen = "screen";
        volume = "volume";
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenConfigUrlIsNull() {
        new S2SAgent(null, "mediaId", streamPositionCallback, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenMediaIdIsNull() {
        new S2SAgent("configUrl", null, streamPositionCallback, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenContextIsNull() {
        new S2SAgent("configUrl", "mediaId", streamPositionCallback, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenConfigUrlIsNull_deprecated() {
        new S2SAgent(null, "mediaId", videoPositionCallback, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenMediaIdIsNull_deprecated() {
        new S2SAgent("configUrl", null, videoPositionCallback, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSSAAgentThrowsExceptionWhenContextIsNull_deprecated() {
        new S2SAgent("configUrl", "mediaId", videoPositionCallback, null);
    }

    @Test
    public void testImpressionNoCallback() {
        S2SAgent agent = new S2SAgent(unitTestConfigUrl, "123", context);
        HashMap errors = agent.impression(contentId, null);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamOnDemandWithoutVolumeAndScreen() {
        Map<String, String> options = new HashMap<>();
        Map errors = s2SAgent.playStreamOnDemand(contentId, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamOnDemandWithoutScreen() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "100");
        Map errors = s2SAgent.playStreamOnDemand(contentId, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamOnDemand() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "0");
        options.put("screen", "fullscreen");
        Map errors = s2SAgent.playStreamOnDemand(contentId, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamOnDemandWithEmptyScreen() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "100");
        options.put("screen", "");
        Map errors = s2SAgent.playStreamOnDemand(contentId, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamLiveWithoutVolume() {
        Map<String, String> options = new HashMap<>();
        options.put("screen", "100");
        Map errors = s2SAgent.playStreamLive(contentId, "", 0, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamLiveWithoutVolumeAndScreen() {
        Map<String, String> options = new HashMap<>();
        Map errors = s2SAgent.playStreamLive(contentId, "", 0, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamLiveWithoutScreen() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "100");
        Map errors = s2SAgent.playStreamLive(contentId, "", 0, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamLive() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "0");
        options.put("screen", "fullscreen");
        Map errors = s2SAgent.playStreamLive(contentId, "", 0, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayStreamLiveWithEmptyScreen() {
        Map<String, String> options = new HashMap<>();
        options.put("volume", "100");
        options.put("screen", "");
        Map errors = s2SAgent.playStreamLive(contentId, "", 0, streamId, options, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlay() {
        HashMap errors = s2SAgent.playVOD(contentId, streamId, screen, "volume", customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayHasErrorWhenContentIdIsIsEmpty() {
        HashMap errors = s2SAgent.playVOD(null, streamId, screen, volume, customParams);
        assertThat(errors).hasSize(1);
        assertThat(errors.containsKey("contentId")).isTrue();
    }

    @Test
    public void testPlayHasErrorWhenStreamIdIsIsEmpty() {
        HashMap errors = s2SAgent.playVOD(contentId, "", screen, volume, customParams);
        assertThat(errors).hasSize(1);
        assertThat(errors.containsKey("streamId")).isTrue();
    }

    @Test
    public void testPlayHasNoErrorWhenScreenIsIsEmpty() {
        HashMap errors = s2SAgent.playVOD(contentId, streamId, "", volume, customParams);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testPlayHasNoErrorWhenVolumeIsIsEmpty() {
        HashMap errors = s2SAgent.playVOD(contentId, streamId, screen, "", customParams);
        assertThat(errors).hasSize(0);
    }


    @Test
    public void testStop() {
        HashMap errors = s2SAgent.stop();
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testSkip() throws InterruptedException {
        simulatePlay();
        HashMap errors = s2SAgent.skip();
        Thread.sleep(100);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testVolume() {
        HashMap errors = s2SAgent.volume("volume");
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testVolumeHasErrorWhenVolumesEmpty() {
        HashMap errors = s2SAgent.volume(null);
        assertThat(errors).hasSize(1);
        assertThat(errors.containsKey("volume")).isTrue();
    }

    @Test
    public void testScreen() {
        HashMap errors = s2SAgent.screen(screen);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void testScreenHasErrorWhenScreenIsEmpty() {
        HashMap errors = s2SAgent.screen(null);
        assertThat(errors).hasSize(1);
        assertThat(errors.containsKey("screen")).isTrue();
    }

    @Test
    public void testFlushStorageWorks() {
        Processor mockedProcessor = mock(Processor.class);
        s2SAgent.setProcessor(mockedProcessor);
        s2SAgent.flushEventStorage();
        verify(mockedProcessor, times(1)).flushStorageQueue();
    }

    @Test
    public void testPlayLiveWorks() {
        Processor mockedProcessor = mock(Processor.class);
        s2SAgent.setProcessor(mockedProcessor);
        Map<String, String> options = new HashMap<>();
        options.put("volume", "100");
        options.put("screen", "full");
        options.put("devicetype", "TV");

        s2SAgent.playLive("sdfasdf", "", 0, "sdfsadfa", "full", "100", customParams);
        verify(mockedProcessor, times(1)).createEventPlayLive(anyString(), anyString(), anyInt(), anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void simulatePlay() {
        streamPosition = 20;
        s2SAgent.playVOD("123", "Bundesliga Bla Bla", screen, volume, customParams);
        streamPosition = 50;
    }
}
