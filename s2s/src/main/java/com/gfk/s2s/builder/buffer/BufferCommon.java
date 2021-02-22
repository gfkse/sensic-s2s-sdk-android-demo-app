package com.gfk.s2s.builder.buffer;

public abstract class BufferCommon {
    protected final long timestamp;

    BufferCommon() {
        timestamp = System.currentTimeMillis();
    }
}
