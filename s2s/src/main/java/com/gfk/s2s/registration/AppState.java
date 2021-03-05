package com.gfk.s2s.registration;

public enum AppState {
    SHOULD_CONNECT("ShouldConnect"),
    CONNECTED("Connected"),
    SHOULD_DISCONNECT("ShouldDisconnect"),
    DISCONNECTED("Disconnected");

    private final String text;

    AppState(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
