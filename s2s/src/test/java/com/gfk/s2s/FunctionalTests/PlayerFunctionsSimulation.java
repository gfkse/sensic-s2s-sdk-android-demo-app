package com.gfk.s2s.FunctionalTests;

import com.gfk.s2s.builder.SegmentBuilder;
import com.gfk.s2s.builder.segment.ISegment;
import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.Config;
import com.gfk.s2s.collector.ICollectorConfigCallback;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;

import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerFunctionsSimulation {

    private int streamOffset;
    private SegmentBuilder segmentBuilder;
    private ISegment segment;
    private PlayerFunctionsCallbacks playerFunctionsCallbacks;

    final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";

    public PlayerFunctionsSimulation(final PlayerFunctionsCallbacks playerFunctionsCallbacks) {
        this.playerFunctionsCallbacks = playerFunctionsCallbacks;

        final Collector collector = new Collector(RuntimeEnvironment.application);
        collector.loadConfig(unitTestConfigUrl, new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                segmentBuilder = new SegmentBuilder(collector.getSegmentConfig());
                segmentBuilder.setStreamPositionCallback(new IStreamPositionCallback() {
                    @Override
                    public long getStreamPosition() {
                        return playerFunctionsCallbacks.getStreamPosition();
                    }
                });
                segmentBuilder.disableDurationChecking();

                streamOffset = 0;
            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void streamStream(String title, int offset, Integer position) {
        if (position != null) {
            playerFunctionsCallbacks.setStreamPosition(position);
        }

        streamOffset = offset;
        segment = null;
        segment = segmentBuilder.createSegmentStarting(streamOffset, title, null);

        assertThat(segment instanceof ISegment).isTrue();
    }

    public void seekForwardStream(int ms, String title) {
        segment = null;
        segment = segmentBuilder.createSegmentStopping(null);

        playerFunctionsCallbacks.setStreamPosition(playerFunctionsCallbacks.getStreamPosition() + ms);

        segment = null;
        segment = segmentBuilder.createSegmentStarting(streamOffset, title, null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void seekBackwardStream(int ms, String title) {
        segment = null;
        segment = segmentBuilder.createSegmentStopping(null);
        assertThat(segment instanceof ISegment).isTrue();

        playerFunctionsCallbacks.setStreamPosition(playerFunctionsCallbacks.getStreamPosition() - ms);

        segment = null;
        segment = segmentBuilder.createSegmentStarting(streamOffset, title, null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void pauseLiveStream(int ms, String title) {
        segment = null;
        segment = segmentBuilder.createSegmentStopping(null);
        assertThat(segment instanceof ISegment).isTrue();

        streamOffset += ms;
        runningStream(ms);

        segment = null;
        segment = segmentBuilder.createSegmentStarting(streamOffset, title, null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void stopStream() {
        segment = null;
        segment = segmentBuilder.createSegmentStopping(null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void changeVolume() {
        segment = null;
        segment = segmentBuilder.createSegmentRunning(null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void changeScreen() {
        segment = null;
        segment = segmentBuilder.createSegmentRunning(null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void triggerHeartbeat() {
        segment = null;
        segment = segmentBuilder.createSegmentRunning(null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void triggerVisibility() {
        segment = null;
        segment = segmentBuilder.createSegmentRunning(null);
        assertThat(segment instanceof ISegment).isTrue();
    }

    public void runningStream(int ms) {
        playerFunctionsCallbacks.setStreamPosition(playerFunctionsCallbacks.getStreamPosition() + ms);
    }

    public void isSegmentDuration(int ms) {
        assertThat(segment.getSegmentDuration()).isEqualTo(ms);
    }

    public void isStreamPosition(int ms) {
        assertThat(segment.getStreamPosition()).isEqualTo(ms);
    }
}
