package com.gfk.s2s.processor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.BufferBuilder;
import com.gfk.s2s.builder.Builder;
import com.gfk.s2s.builder.buffer.BufferCommon;
import com.gfk.s2s.builder.buffer.BufferImpression;
import com.gfk.s2s.builder.event.EventImpression;
import com.gfk.s2s.builder.event.EventPlay;
import com.gfk.s2s.builder.event.EventScreen;
import com.gfk.s2s.builder.event.EventSkip;
import com.gfk.s2s.builder.event.EventStop;
import com.gfk.s2s.builder.event.EventVolume;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.segment.ISegment;
import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.Config;
import com.gfk.s2s.collector.ICollectorConfigCallback;
import com.gfk.s2s.collector.ICollectorSuiAvailableCallback;
import com.gfk.s2s.s2sagent.StreamPositionCallback;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;
import com.gfk.s2s.streamPositionManager.StreamPositionManager;
import com.gfk.s2s.transmitter.ExpBackoffTransmitter;
import com.gfk.s2s.transmitter.ITransmitter;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Processor {
    private static final int FLUSH_STORAGE_PERIOD_IN_SECONDS = 20;
    private final BufferBuilder bufferBuilder;
    private Collector collector;
    private Builder builder;
    private ITransmitter transmitter;
    private StreamPositionManager streamPositionManager;
    private boolean configLoaded = false;
    private StreamPositionCallback streamPositionCallback;
    private ScheduledFuture<?> flushStorageExecutor;
    private LinkedList<BufferCommon> bufferStorage;

    public Processor(String configUrl, final String mediaId, Boolean optin, final StreamPositionCallback streamPositionCallback, final Context context) {
        collector = new Collector(context);
        streamPositionManager = new StreamPositionManager(collector);
        bufferBuilder = new BufferBuilder();
        bufferStorage = new LinkedList<>();
        this.streamPositionCallback = streamPositionCallback;
        useStreamOnDemandPositionCallbackBufferBuilder();
        ICollectorSuiAvailableCallback suiCallback = this::flushStorageQueue;
        collector.setCollectorSuiCallback(suiCallback);
        ScheduledExecutorService flushStorageExecutorService = Executors.newScheduledThreadPool(1);
        flushStorageExecutor = flushStorageExecutorService.schedule(() -> {
            if (collector.getSui().isEmpty())
                flushStorageQueue();
        }, FLUSH_STORAGE_PERIOD_IN_SECONDS, TimeUnit.SECONDS);
        collector.setOptin(optin);

        collector.loadConfig(configUrl, new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                if (success) {
                    transmitter = new ExpBackoffTransmitter(collector.getTrackingUrl());
                    builder = new Builder(mediaId, collector);
                    configLoaded = true;
                }
            }

            @Override
            public void onFinished() {
                loadSui();
            }
        });
    }

    LinkedList<BufferCommon> getBufferStorage() {
        return bufferStorage;
    }

    void setTransmitter(ITransmitter transmitter) {
        this.transmitter = transmitter;
    }

    @VisibleForTesting
    void setSUI(String sui) {
        collector.setSUI(sui);
    }

    private void loadSui() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable runner = () -> collector.loadSui();
        mainHandler.post(runner);
    }

    private void useStreamLivePositionCallbackSegmentBuilder() {
        IStreamPositionCallback streamPositionCallbackTmp = streamPositionManager.getExtendedStreamPositionCallbackLive();
        builder.setStreamPositionCallback(streamPositionCallbackTmp);
    }

    private void useStreamOnDemandPositionCallbackSegmentBuilder() {
        IStreamPositionCallback streamPositionCallbackTmp = streamPositionManager.getExtendedStreamPositionCallbackOnDemand(streamPositionCallback);
        builder.setStreamPositionCallback(streamPositionCallbackTmp);
    }

    private void useStreamLivePositionCallbackBufferBuilder() {
        IStreamPositionCallback streamPositionCallbackTmp = streamPositionManager.getExtendedStreamPositionCallbackLive();
        bufferBuilder.setStreamPositionCallback(streamPositionCallbackTmp);
    }

    private void useStreamOnDemandPositionCallbackBufferBuilder() {
        IStreamPositionCallback streamPositionCallbackTmp = streamPositionManager.getExtendedStreamPositionCallbackOnDemand(streamPositionCallback);
        bufferBuilder.setStreamPositionCallback(streamPositionCallbackTmp);
    }

    public void setStreamPositionCallback(StreamPositionCallback streamPositionCallback) {
        this.streamPositionCallback = streamPositionCallback;
        useStreamOnDemandPositionCallbackBufferBuilder();
    }

    public void flushStorageQueue() {
        bufferBuilder.mergeBufferEvents(bufferStorage, this);
        flushStorageExecutor.cancel(true);
    }

    public void createEventImpression(String contentId, Map customParams) {
        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            EventImpression event = builder.getEventBuilder().buildEventImpression(contentId, customParams);
            transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
        } else {
            BufferImpression event = bufferBuilder.buildBufferImpression(contentId, customParams);
            bufferStorage.add(event);
        }
    }

    public void createEventPlayOnDemand(String contentId, String streamId, IEventPlayOptions options, Map customParams, Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();
        if(!options.getDeviceType().isEmpty()){
            collector.setDeviceType(options.getDeviceType());
        }
        else{
            collector.setDeviceType("UNKNOWN");
        }
        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            useStreamOnDemandPositionCallbackSegmentBuilder();
            ISegment segment = builder.getSegmentBuilder().createSegmentStarting(0, streamId, bufferStreamPosition);
            if (segment != null) {
                EventPlay event = builder.getEventBuilder().buildEventPlay(segment, "", 0, usageTime, contentId, AllowedPlayType.ondemand, options, customParams);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            useStreamOnDemandPositionCallbackBufferBuilder();
            BufferCommon bufferCommon = bufferBuilder.buildBufferPlay(contentId, streamId, 0, options, customParams, "", usageTime, AllowedPlayType.ondemand);
            bufferStorage.add(bufferCommon);
        }
    }

    public void createEventPlayLive(String contentId, String streamStartTime, int streamOffset, String streamId, IEventPlayOptions options, Map customParams, Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();
        if(!options.getDeviceType().isEmpty()){
            collector.setDeviceType(options.getDeviceType());
        }
        else{
            collector.setDeviceType("UNKNOWN");
        }
        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            useStreamLivePositionCallbackSegmentBuilder();
            ISegment segment = builder.getSegmentBuilder().createSegmentStarting(streamOffset, streamId, bufferStreamPosition);
            if (segment != null) {
                EventPlay event = builder.getEventBuilder().buildEventPlay(segment, streamStartTime, streamOffset, usageTime, contentId, AllowedPlayType.live, options, customParams);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            useStreamLivePositionCallbackBufferBuilder();
            BufferCommon bufferCommon = bufferBuilder.buildBufferPlay(contentId, streamId, streamOffset, options, customParams, streamStartTime, usageTime, AllowedPlayType.live);
            bufferStorage.add(bufferCommon);
        }
    }

    public void createEventStop(Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();

        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            ISegment segment = builder.getSegmentBuilder().createSegmentStopping(bufferStreamPosition);
            if (segment != null) {
                EventStop event = builder.getEventBuilder().buildEventStop(segment, usageTime);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            BufferCommon bufferCommon = bufferBuilder.buildBufferStop(usageTime);
            bufferStorage.add(bufferCommon);
        }
    }

    public void createEventSkip(Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();

        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            ISegment segment = builder.getSegmentBuilder().createSegmentStopping(bufferStreamPosition);
            if (segment != null) {
                EventSkip event = builder.getEventBuilder().buildEventSkip(segment, usageTime);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            BufferCommon bufferCommon = bufferBuilder.buildBufferSkip(usageTime);
            bufferStorage.add(bufferCommon);
        }
    }

    public void createEventVolume(String volume, Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();

        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            ISegment segment = builder.getSegmentBuilder().createSegmentRunning(bufferStreamPosition);
            if (segment != null) {
                EventVolume event = builder.getEventBuilder().buildEventVolume(segment, volume, usageTime);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            BufferCommon bufferCommon = bufferBuilder.buildBufferVolume(volume, usageTime);
            bufferStorage.add(bufferCommon);
        }
    }

    public void createEventScreen(String screen, Long bufferStreamPosition) {
        long usageTime = collector.getTimeWithOffset();

        if (sdkFullyLoaded()) {
            if (!collector.isProjectEnabled()) return;
            ISegment segment = builder.getSegmentBuilder().createSegmentRunning(bufferStreamPosition);
            if (segment != null) {
                EventScreen event = builder.getEventBuilder().buildEventScreen(segment, screen, usageTime);
                transmitter.sendRequest(builder.getRequestBuilder().buildRequest(event));
            }
        } else {
            BufferCommon bufferCommon = bufferBuilder.buildBufferScreen(screen, usageTime);
            bufferStorage.add(bufferCommon);
        }
    }

    private boolean sdkFullyLoaded() {
        return this.builder != null && configLoaded && !collector.getSui().isEmpty();
    }

    void disableDurationChecking() {
        builder.getSegmentBuilder().disableDurationChecking();
    }

    boolean isProjectEnabled() {
        return (collector.isProjectEnabled() == null) ? true : collector.isProjectEnabled();
    }
}
