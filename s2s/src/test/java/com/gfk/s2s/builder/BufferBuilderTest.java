package com.gfk.s2s.builder;

import com.gfk.s2s.builder.buffer.BufferCommon;
import com.gfk.s2s.builder.buffer.BufferImpression;
import com.gfk.s2s.builder.buffer.BufferPlay;
import com.gfk.s2s.builder.buffer.BufferScreen;
import com.gfk.s2s.builder.buffer.BufferSkip;
import com.gfk.s2s.builder.buffer.BufferStop;
import com.gfk.s2s.builder.buffer.BufferVolume;
import com.gfk.s2s.builder.event.EventBase;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.processor.Processor;
import com.gfk.s2s.s2sagent.StreamPositionCallback;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;
import com.gfk.s2s.transmitter.ITransmitter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(LibTestRunner.class)
public class BufferBuilderTest {

    private Map customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "qwe");}};
    private Map<String, String> options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "100"); put("deviceType", "TV");}};
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
    private BufferBuilder builder;
    Processor mockProcessor;
    private long streamPosition;

    @Before
    public void setUp() {
        builder = new BufferBuilder();
        builder.setStreamPositionCallback(() -> streamPosition);
        mockProcessor = mock(Processor.class);
        streamPosition = 0;
    }

    @Test
    public void mergeBufferVODEvents() {
        BufferPlay bufferPlay = builder.buildBufferPlay("c1","s1", 0, playOptions, customParameters, "", 2000, AllowedPlayType.ondemand);
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferPlay);
        BufferPlay bufferPlay2 = builder.buildBufferPlay("c2","s2", 0, playOptions, customParameters, "", 2000, AllowedPlayType.ondemand);
        bufferStorage.add(bufferPlay2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventPlayOnDemand("c1", "s1", playOptions, customParameters, 0L);
        verify(mockProcessor, times(1)).createEventPlayOnDemand("c2", "s2", playOptions, customParameters, 0L);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferLiveEvents() {
        streamPosition = 100;
        BufferPlay bufferPlay = builder.buildBufferPlay("c1","s1", 0, playOptions, customParameters, "", 2000, AllowedPlayType.live);
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferPlay);
        BufferPlay bufferPlay2 = builder.buildBufferPlay("c2","s2", 0, playOptions, customParameters, "", 2000, AllowedPlayType.live);
        bufferStorage.add(bufferPlay2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventPlayLive("c1", "", 0, "s1", playOptions, customParameters, streamPosition);
        verify(mockProcessor, times(1)).createEventPlayLive("c2", "", 0, "s2", playOptions, customParameters, streamPosition);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferSkipEvents() {
        streamPosition = 0;
        BufferSkip bufferSkip = builder.buildBufferSkip(10L);
        streamPosition = 10;
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferSkip);
        BufferSkip bufferSkip2 = builder.buildBufferSkip(15L);
        bufferStorage.add(bufferSkip2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventSkip(0L);
        verify(mockProcessor, times(1)).createEventSkip(10L);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferVolumeEvents() {
        streamPosition = 0;
        BufferVolume bufferVolume = builder.buildBufferVolume("50", 10L);
        streamPosition =10;
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferVolume);
        BufferVolume bufferVolume2 = builder.buildBufferVolume("100", 10L);
        bufferStorage.add(bufferVolume2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventVolume("50",0L);
        verify(mockProcessor, times(1)).createEventVolume("100",10L);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferImpressionEvents() {
        BufferImpression bufferImpression = builder.buildBufferImpression("1", customParameters);
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferImpression);
        BufferImpression bufferImpression2 = builder.buildBufferImpression("2", customParameters);
        bufferStorage.add(bufferImpression2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventImpression("1",customParameters);
        verify(mockProcessor, times(1)).createEventImpression("2",customParameters);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferStopEvents() {
        streamPosition = 0;
        BufferStop bufferStop1 = builder.buildBufferStop(0);
        streamPosition = 100;
        BufferStop bufferStop2 = builder.buildBufferStop(0);
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferStop1);
        bufferStorage.add(bufferStop2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventStop(0L);
        verify(mockProcessor, times(1)).createEventStop(100L);
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void mergeBufferScreenEvents() {
        streamPosition = 0;
        BufferScreen bufferScreen1 = builder.buildBufferScreen("fullscreen", 0);
        streamPosition = 100;
        BufferScreen bufferScreen2 = builder.buildBufferScreen("microwave", 0);
        LinkedList<BufferCommon> bufferStorage = new LinkedList<>();
        bufferStorage.add(bufferScreen1);
        bufferStorage.add(bufferScreen2);
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
        verify(mockProcessor, times(1)).createEventScreen("fullscreen", 0L);
        verify(mockProcessor, times(1)).createEventScreen("microwave", 100L);
        assertTrue(bufferStorage.isEmpty());
    }
}
