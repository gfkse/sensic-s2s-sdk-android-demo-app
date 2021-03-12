package com.gfk.s2s.builder;

import android.util.Log;

import com.gfk.s2s.builder.buffer.BufferCommon;
import com.gfk.s2s.builder.buffer.BufferImpression;
import com.gfk.s2s.builder.buffer.BufferPlay;
import com.gfk.s2s.builder.buffer.BufferScreen;
import com.gfk.s2s.builder.buffer.BufferSkip;
import com.gfk.s2s.builder.buffer.BufferStop;
import com.gfk.s2s.builder.buffer.BufferVolume;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.processor.Processor;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;
import com.gfk.s2s.utils.GlobalConst;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class BufferBuilder {

    private IStreamPositionCallback streamPositionCallback;

    public void setStreamPositionCallback(IStreamPositionCallback streamPositionCallback) {
        this.streamPositionCallback = streamPositionCallback;
    }

    public BufferImpression buildBufferImpression(String contentId, Map customParams) {
        return new BufferImpression(contentId, customParams);
    }

    public BufferPlay buildBufferPlay(String contentId, String streamId, int streamOffset, IEventPlayOptions options, Map customParameters, String streamStartTime, long usageTime, AllowedPlayType playType) {
        return new BufferPlay(contentId, streamOffset, streamId, options, customParameters, streamPositionCallback.getStreamPosition(), streamStartTime, usageTime, playType);
    }

    public BufferStop buildBufferStop(long usageTime) {
        return new BufferStop(streamPositionCallback.getStreamPosition(), usageTime);
    }

    public BufferSkip buildBufferSkip(long usageTime) {
        return new BufferSkip(streamPositionCallback.getStreamPosition(), usageTime);
    }

    public BufferScreen buildBufferScreen(String screen, long usageTime) {
        return new BufferScreen(screen, streamPositionCallback.getStreamPosition(), usageTime);
    }

    public BufferVolume buildBufferVolume(String volume, long usageTime) {
        return new BufferVolume(volume, streamPositionCallback.getStreamPosition(), usageTime);
    }

    synchronized public void mergeBufferEvents(LinkedList<BufferCommon> bufferStorage, Processor processor) {
        Set<BufferCommon> removeSet = new HashSet<>();
        for (BufferCommon currentItem: bufferStorage) {
            Class c = currentItem.getClass();
            if (c == BufferPlay.class) {
                BufferPlay bufferPlay = (BufferPlay) currentItem;
                if (bufferPlay.getPlayType() == AllowedPlayType.live) {
                    processor.createEventPlayLive(bufferPlay.getContentId(), bufferPlay.getStreamStartTime(), bufferPlay.getStreamOffset(), bufferPlay.getStreamId(), bufferPlay.getOptions(), bufferPlay.getCustomParams(), bufferPlay.getStreamPosition());
                } else {
                    processor.createEventPlayOnDemand(bufferPlay.getContentId(), bufferPlay.getStreamId(), bufferPlay.getOptions(), bufferPlay.getCustomParams(), bufferPlay.getStreamPosition());
                }
            } else if (c == BufferStop.class) {
                BufferStop bufferStop = (BufferStop) currentItem;
                processor.createEventStop(bufferStop.getStreamPosition());
            } else if (c == BufferSkip.class) {
                BufferSkip bufferSkip = (BufferSkip) currentItem;
                processor.createEventSkip(bufferSkip.getStreamPosition());
            } else if (c == BufferVolume.class) {
                BufferVolume bufferVolume = (BufferVolume) currentItem;
                processor.createEventVolume(bufferVolume.getVolume(), bufferVolume.getStreamPosition());
            } else if (c == BufferScreen.class) {
                BufferScreen bufferScreen = (BufferScreen) currentItem;
                processor.createEventScreen(bufferScreen.getScreen(), bufferScreen.getStreamPosition());
            } else if (c == BufferImpression.class) {
                BufferImpression bufferImpression = (BufferImpression) currentItem;
                processor.createEventImpression(bufferImpression.getContentId(), bufferImpression.getCustomParams());
            } else {
                Log.e(GlobalConst.LOG_TAG, "Element is not handled here");
            }
            removeSet.add(currentItem);
        }
        bufferStorage.removeAll(removeSet);
    }
}
