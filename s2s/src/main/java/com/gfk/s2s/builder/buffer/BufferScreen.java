package com.gfk.s2s.builder.buffer;

public class BufferScreen extends BufferBase {
    private final String screen;

    public BufferScreen(String screen, long streamPosition, long usageTime) {
        super(streamPosition, usageTime);
        this.screen = screen;
    }

    public String getScreen() {
        return screen;
    }
}
