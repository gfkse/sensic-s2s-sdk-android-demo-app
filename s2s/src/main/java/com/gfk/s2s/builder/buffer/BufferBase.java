package com.gfk.s2s.builder.buffer;

public abstract class BufferBase extends BufferCommon {
    protected final long streamPosition;
    private final long usageTime;

    public BufferBase(long streamPosition, long usageTime) {
        this.streamPosition = streamPosition;
        this.usageTime = usageTime;
    }

    public long getStreamPosition() { return streamPosition; }

    public long getUsageTime() { return usageTime; }
}
