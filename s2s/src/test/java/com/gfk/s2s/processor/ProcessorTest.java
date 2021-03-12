package com.gfk.s2s.processor;

import android.content.Context;

import com.gfk.s2s.builder.buffer.BufferImpression;
import com.gfk.s2s.builder.buffer.BufferPlay;
import com.gfk.s2s.builder.buffer.BufferScreen;
import com.gfk.s2s.builder.buffer.BufferSkip;
import com.gfk.s2s.builder.buffer.BufferStop;
import com.gfk.s2s.builder.buffer.BufferVolume;
import com.gfk.s2s.builder.event.EventVolume;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.builder.request.RequestBase;
import com.gfk.s2s.builder.request.RequestCommon;
import com.gfk.s2s.s2sagent.StreamPositionCallback;
import com.gfk.s2s.transmitter.ITransmitter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(LibTestRunner.class)
public class ProcessorTest {

    private static final String contentId = "contentId";
    private static final String streamId = "streamId";
    private static final String screen = "screen";
    private static final String volume = "volume";
    private static HashMap<String, String> options;
    private int streamPosition = 0;
    private HashMap customParams;
    private final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";
    private Context context;
    private StreamPositionCallback streamPositionCallback;
    private Processor processor;
    private ITransmitter mockTransmitter;

    @Before
    public void setup() {
        customParams = new HashMap();

        options = new HashMap<>();
        options.put("screen", "100");
        options.put("volume", "20");
        options.put("deviceType", "TV");

        context = RuntimeEnvironment.application;
        streamPositionCallback = () -> streamPosition;

        processor = new Processor(unitTestConfigUrl, "123", true, streamPositionCallback, context);
        processor.disableDurationChecking();
        processor.setSUI("2342524535345");
        mockTransmitter = mock(ITransmitter.class);
        processor.setTransmitter(mockTransmitter);
    }

    @Test
    public void testDeviceTypeSetByOptionsLive() {
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
            public String getDeviceType() { return options.get("deviceType"); }
        };
        processor.createEventPlayLive(contentId, "", 0, streamId, playOptions, customParams, null);
        assertThatContains("dt=TV", 1);
    }

    @Test
    public void testDeviceTypeSetByOptionsOnDemand() {
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
            public String getDeviceType() { return options.get("deviceType"); }
        };
        processor.createEventPlayOnDemand(contentId, streamId, playOptions, customParams, null);
        assertThatContains("dt=TV", 1);
    }

    @Test
    public void testDeviceTypeSetByDefaultLive() {
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
            public String getDeviceType() { return ""; }
        };
        processor.createEventPlayLive(contentId, "", 0, streamId, playOptions, customParams, null);
        assertThatContains("dt=SMARTPHONE", 1);
    }

    @Test
    public void testDeviceTypeSetByDefaultOnDemand() {
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
            public String getDeviceType() { return ""; }
        };
        processor.createEventPlayOnDemand(contentId, streamId, playOptions, customParams, null);
        assertThatContains("dt=SMARTPHONE", 1);
    }

    @Test
    public void testImpressionEvent() {
        processor.createEventImpression(contentId, customParams);
        assertThatContains("ty=IM", 1);
        assertThatContains("c=contentId", 1);
    }

    @Test
    public void tempImpressionBuffer() throws InterruptedException {
        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);

        configlessProcessor.createEventImpression(contentId, customParams);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
        BufferImpression bufferImpression = (BufferImpression) configlessProcessor.getBufferStorage().get(0);
        assertThat(bufferImpression.getContentId()).isEqualTo(contentId);
        assertThat(bufferImpression.getCustomParams()).isEqualTo(customParams);
    }

    @Test
    public void testPlayEvent() {
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
            public String getDeviceType() { return options.get("deviceType"); }
        };

        processor.createEventPlayLive(contentId, "", 0, streamId, playOptions, customParams, null);
        assertThatContains("ty=PL", 1);
        assertThatContains("c=contentId", 1);
    }

    @Test
    public void testStopEvent() {
        simulatePlay();
        processor.createEventStop(1000000L);
        assertThatContains("ty=ST", 2);
    }

    @Test
    public void testSkipEvent() {
        simulatePlay();
        processor.createEventSkip(1000000L);
        assertThatContains("ty=ST", 2);
    }

    @Test
    public void tempPlayBuffer() throws InterruptedException {
        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);

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
            public String getDeviceType() { return options.get("deviceType"); }
        };

        configlessProcessor.createEventPlayOnDemand(contentId, streamId, playOptions, customParams, null);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
        BufferPlay bufferPlay = (BufferPlay) configlessProcessor.getBufferStorage().get(0);
        assertThat(bufferPlay.getContentId()).isEqualTo(contentId);
        assertThat(bufferPlay.getStreamOffset()).isEqualTo(0);
        assertThat(bufferPlay.getStreamId()).isEqualTo(streamId);
        assertThat(bufferPlay.getOptions()).isEqualTo(playOptions);
        assertThat(bufferPlay.getCustomParams()).isEqualTo(customParams);
    }

    @Test
    public void tempStopBuffer() throws InterruptedException {
        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);

        configlessProcessor.createEventStop(null);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
    }

    @Test
    public void testSkipBuffer() throws InterruptedException {
        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);

        configlessProcessor.createEventSkip(null);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
    }

    @Test
    public void testVolumeEvent() {
        simulatePlay();
        processor.createEventVolume("34", null);

        assertThatContains("ty=VO", 2);
    }

    @Test
    public void testVolumeBuffer() throws InterruptedException {
        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);
        configlessProcessor.createEventVolume(volume, null);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
        BufferVolume bufferVolume = (BufferVolume) configlessProcessor.getBufferStorage().get(0);
        assertThat(bufferVolume.getVolume()).isEqualTo(volume);
    }

    @Test
    public void testScreenEvent() {
        simulatePlay();
        processor.createEventScreen("1", null);
        assertThatContains("ty=SC", 2);
    }

    @Test
    public void testScreenBuffer() throws InterruptedException {
        final BufferScreen[] bufferScreens = {null};

        Processor configlessProcessor = new Processor("http://localhost:8881/dev-s2s.json-missing", "123", true, streamPositionCallback, context);

        configlessProcessor.createEventScreen(screen, null);

        assertThat(configlessProcessor.getBufferStorage().size()).isEqualTo(1);
        BufferScreen bufferScreen = (BufferScreen) configlessProcessor.getBufferStorage().get(0);
        assertThat(bufferScreen.getScreen()).isEqualTo(screen);
    }

    @Test
    public void testProcessorEnabledConfig() throws InterruptedException {
        Processor processor = new Processor(unitTestConfigUrl, "123", true, streamPositionCallback, context);

        Thread.sleep(50);

        assertThat(processor.isProjectEnabled()).isTrue();
    }

    @Test
    public void testProcessorDisabledConfig() throws InterruptedException {
        String url = "http://localhost:8881/s2s-android-unittest-disabled.json";

        Processor processor = new Processor(url, "123", true, streamPositionCallback, context);

        Thread.sleep(50);

        assertThat(processor.isProjectEnabled()).isFalse();
    }

    private void simulatePlay() {
        streamPosition = 20;
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
        processor.createEventPlayOnDemand(contentId, streamId, playOptions, customParams, null);
        streamPosition = 5000;
    }

    private void assertThatContains(String expectedValue, int invocations) {
        ArgumentCaptor<IRequest> parameterCaptor = ArgumentCaptor.forClass(IRequest.class);
        verify(mockTransmitter, times(invocations)).sendRequest(parameterCaptor.capture());
        IRequest value = parameterCaptor.getValue();
        assertTrue(value.getAsUrlString().contains(expectedValue));
    }
}