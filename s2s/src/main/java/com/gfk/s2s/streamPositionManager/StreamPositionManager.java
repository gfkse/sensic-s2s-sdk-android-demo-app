package com.gfk.s2s.streamPositionManager;

import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.s2sagent.StreamPositionCallback;

public class StreamPositionManager {

    private Collector collector;

    public StreamPositionManager(Collector collector) {
        this.collector = collector;
    }

    public IStreamPositionCallback getExtendedStreamPositionCallbackOnDemand(final StreamPositionCallback streamPositionCallback) {
        return () -> {
            long position = Long.parseLong(String.valueOf(streamPositionCallback.onCallback()));

            int minMilliseconds = 1000;
            if (position < minMilliseconds) {
                return 0;
            }

            return position;
        };
    }

    public IStreamPositionCallback getExtendedStreamPositionCallbackLive() {
        return collector::getTimeWithOffset;
    }
}
