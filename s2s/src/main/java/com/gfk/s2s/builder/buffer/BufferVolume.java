package com.gfk.s2s.builder.buffer;

public class BufferVolume extends BufferBase {
    private final String volume;

    public BufferVolume(String volume, long streamPosition, long usageTime) {
        super(streamPosition, usageTime);
        this.volume = volume;
    }

    public String getVolume() {
        return volume;
    }
}
