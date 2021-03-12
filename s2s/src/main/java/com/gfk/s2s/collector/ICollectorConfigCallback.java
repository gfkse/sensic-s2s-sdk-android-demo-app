package com.gfk.s2s.collector;

public interface ICollectorConfigCallback {
    void onCompletion(Config config, boolean success);
    void onFinished();
}
