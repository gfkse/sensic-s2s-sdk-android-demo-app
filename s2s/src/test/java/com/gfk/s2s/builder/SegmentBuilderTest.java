package com.gfk.s2s.builder;

import com.gfk.s2s.builder.segment.ISegment;
import com.gfk.s2s.builder.segment.Segment;
import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.Config;
import com.gfk.s2s.collector.ICollectorConfigCallback;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(LibTestRunner.class)
public class SegmentBuilderTest {

    final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";

    private int streamOffset = 23000;
    private String streamId = "stream title";
    String streamId2 = "stream title 2";
    int streamPositionStart1 = 0;
    int streamPositionStop1 = 85000;
    int streamPositionStart2 = 120000;
    int streamPositionStop2 = 300000;
    Collector collector = new Collector(RuntimeEnvironment.application);
    private SegmentBuilder segmentBuilder;
    private int streamPosition = 0;

    private final IStreamPositionCallback streamPositionCallback = new IStreamPositionCallback() {

        @Override
        public long getStreamPosition() {
            return streamPosition;
        }
    };

    private int getStreamPosition() {
        return streamPosition;
    }

    private void setStreamPosition(int position) {
        streamPosition = position;
    }

    @Before
    public void setUp() {
        collector.loadConfig(unitTestConfigUrl, new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                segmentBuilder = new SegmentBuilder(collector.getSegmentConfig());
                segmentBuilder.setStreamPositionCallback(streamPositionCallback);
                segmentBuilder.disableDurationChecking();
            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Test
    public void testIfSegmentItemStartingCanBeCreated() throws Exception {
        streamPosition = 0;
        ISegment segment = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);

        assertThat(segment).isInstanceOf(Segment.class);
    }

    @Test
    public void TestIfSegmentItemRunningCantBeCreatedAsFirstSegmentItem() throws Exception {
        streamPosition = 0;
        ISegment segment = segmentBuilder.createSegmentRunning(null);

        assertThat(segment).isNull();
    }

    @Test
    public void TestIfSegmentItemStoppingCantBeCreatedAsFirstSegmentItem() throws Exception {
        streamPosition = 0;
        ISegment segment = segmentBuilder.createSegmentStopping(null);

        assertThat(segment).isNull();
    }

    @Test
    public void testIfSegmentCanBeStoppedAfter2Secs() throws Exception {
        streamPosition = 0;
        segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        streamPosition += 2000;
        ISegment segment = segmentBuilder.createSegmentStopping(null);

        assertThat(segment).isInstanceOf(Segment.class);
    }

    @Test
    public void testIfSegmentCanBeCreatedAndRunningFor2Secs() throws Exception {
        streamPosition = 0;
        segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        streamPosition += 2000;
        ISegment segment = segmentBuilder.createSegmentRunning(null);

        assertThat(segment).isInstanceOf(Segment.class);
    }

    @Test
    public void testIfSegmentCantBeCreatedWithoutStarting() throws Exception {
        streamPosition = 0;
        ISegment segment = segmentBuilder.createSegmentRunning(null);

        assertThat(segment).isNull();
    }

    @Test
    public void testIfTwoSegmentsCanBeCreatedForSameStreamAndIncludesCorrectSegmentNoAndSegmentStateItemNo() throws Exception {
        streamPosition = 0;

        ISegment segmentPlaySegment1 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentVolumeSegment1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentScreenSegment1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentVisibilitySegment1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentStopSegment1 = segmentBuilder.createSegmentStopping(null);
        setStreamPosition(getStreamPosition() + 1000);

        segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        segmentBuilder.createSegmentRunning(null);
        setStreamPosition(0);
        segmentBuilder.createSegmentRunning(null);

        setStreamPosition(0);
        ISegment segmentPlaySegment2 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentHeartbeatSegment2 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentStopSegment2 = segmentBuilder.createSegmentStopping(null);
        setStreamPosition(getStreamPosition() + 1000);
        setStreamPosition(0);

        // REQUEST order
        assertThat(segmentPlaySegment1.getStateItemNumber()).isEqualTo(1);
        assertThat(segmentVolumeSegment1.getStateItemNumber()).isEqualTo(2);
        assertThat(segmentScreenSegment1.getStateItemNumber()).isEqualTo(3);
        assertThat(segmentStopSegment1.getStateItemNumber()).isEqualTo(5);
        assertThat(segmentPlaySegment2.getStateItemNumber()).isEqualTo(6);
        assertThat(segmentHeartbeatSegment2.getStateItemNumber()).isEqualTo(7);
        assertThat(segmentStopSegment2.getStateItemNumber()).isEqualTo(8);

        // SEGMENT 1
        assertThat(segmentVolumeSegment1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentScreenSegment1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentVisibilitySegment1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentPlaySegment1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentStopSegment1.getSegmentNumber()).isEqualTo(1);

        // SEGMENT 2
        assertThat(segmentPlaySegment2.getSegmentNumber()).isEqualTo(2);
        assertThat(segmentStopSegment2.getSegmentNumber()).isEqualTo(2);
    }

    @Test
    public void testIfTwoStreamsAreCreatedCorrectly() throws Exception {
        // STREAM 1 SEGMENT 1
        setStreamPosition(0);
        ISegment segmentPlaySegment1Stream1 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentHeartbeat1Segment1Stream1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentHeartbeat2Segment1Stream1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentHeartbeat3Segment1Stream1 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(getStreamPosition() + 1000);
        ISegment segmentStopSegment1Stream1 = segmentBuilder.createSegmentStopping(null);

        // STREAM 1 SEGMENT 2
        setStreamPosition(0);
        ISegment segmentPlaySegment2Stream1 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentStopSegment2Stream1 = segmentBuilder.createSegmentStopping(null);

        // STREAM 2 SEGMENT 1
        setStreamPosition(0);
        ISegment segmentPlaySegment1Stream2 = segmentBuilder.createSegmentStarting(streamOffset, streamId2, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentStopSegment1Stream2 = segmentBuilder.createSegmentStopping(null);

        // STREAM 2 SEGMENT 2
        setStreamPosition(0);
        ISegment segmentPlaySegment2Stream2 = segmentBuilder.createSegmentStarting(streamOffset, streamId2, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentStopSegment2Stream2 = segmentBuilder.createSegmentStopping(null);

        assertThat(segmentPlaySegment1Stream1.getPresentationId()).isNotEqualTo(segmentPlaySegment1Stream2.getPresentationId());

        // STREAM 1 SEGMENT 1
        assertThat(segmentPlaySegment1Stream1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentHeartbeat1Segment1Stream1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentHeartbeat2Segment1Stream1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentHeartbeat3Segment1Stream1.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentStopSegment1Stream1.getSegmentNumber()).isEqualTo(1);

        // STREAM 1 SEGMENT 2
        assertThat(segmentPlaySegment2Stream1.getSegmentNumber()).isEqualTo(2);
        assertThat(segmentStopSegment2Stream1.getSegmentNumber()).isEqualTo(2);

        // STREAM 2 SEGMENT 1
        assertThat(segmentPlaySegment1Stream2.getSegmentNumber()).isEqualTo(1);
        assertThat(segmentStopSegment1Stream2.getSegmentNumber()).isEqualTo(1);

        // STREAM 2 SEGMENT 2
        assertThat(segmentPlaySegment2Stream2.getSegmentNumber()).isEqualTo(2);
        assertThat(segmentStopSegment2Stream2.getSegmentNumber()).isEqualTo(2);

        // STREAM 1 SEGMENT 1
        assertThat(segmentPlaySegment1Stream1.getStateItemNumber()).isEqualTo(1);
        assertThat(segmentHeartbeat1Segment1Stream1.getStateItemNumber()).isEqualTo(2);
        assertThat(segmentHeartbeat2Segment1Stream1.getStateItemNumber()).isEqualTo(3);
        assertThat(segmentHeartbeat3Segment1Stream1.getStateItemNumber()).isEqualTo(4);
        assertThat(segmentStopSegment1Stream1.getStateItemNumber()).isEqualTo(5);

        // STREAM 1 SEGMENT 2
        assertThat(segmentPlaySegment2Stream1.getStateItemNumber()).isEqualTo(6);
        assertThat(segmentStopSegment2Stream1.getStateItemNumber()).isEqualTo(7);

        // STREAM 2 SEGMENT 1
        assertThat(segmentPlaySegment1Stream2.getStateItemNumber()).isEqualTo(1);
        assertThat(segmentStopSegment1Stream2.getStateItemNumber()).isEqualTo(2);

        // STREAM 2 SEGMENT 2
        assertThat(segmentPlaySegment2Stream2.getStateItemNumber()).isEqualTo(3);
        assertThat(segmentStopSegment2Stream2.getStateItemNumber()).isEqualTo(4);
    }

    @Test
    public void TestIfOneStreamHasSamePresentationIdForDifferentSegments() throws Exception {
        setStreamPosition(0);
        ISegment segmentPlayStream1 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentStopStream1 = segmentBuilder.createSegmentStopping(null);

        setStreamPosition(0);
        ISegment segmentPlayStream2 = segmentBuilder.createSegmentStarting(streamOffset, streamId2, null);
        setStreamPosition(getStreamPosition() + 2000);
        segmentBuilder.createSegmentStopping(null);

        setStreamPosition(0);
        ISegment segmentPlayStream2Segment1 = segmentBuilder.createSegmentStarting(streamOffset, streamId2, null);
        setStreamPosition(getStreamPosition() + 2000);
        ISegment segmentStopStream2Segment1 = segmentBuilder.createSegmentStopping(null);

        // STREAM 1
        assertThat(segmentPlayStream1.getPresentationId()).isEqualTo(segmentStopStream1.getPresentationId());

        // STREAM 2 SEGMENT 1
        assertThat(segmentPlayStream2.getPresentationId()).isEqualTo(segmentPlayStream2Segment1.getPresentationId());

        // STREAM 2 SEGMENT 2
        assertThat(segmentPlayStream2Segment1.getPresentationId()).isEqualTo(segmentStopStream2Segment1.getPresentationId());
        assertThat(segmentPlayStream2Segment1.getPresentationId()).isNotEqualTo(segmentPlayStream1.getPresentationId());

    }

    @Test
    public void testIfViewtimeIsCalculatedCorrectForSameStreamWithTwoSegments() throws Exception {
        setStreamPosition(streamPositionStart1);
        segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(streamPositionStop1);
        ISegment segmentStopSegment1 = segmentBuilder.createSegmentStopping(null);

        setStreamPosition(streamPositionStart2);
        segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(streamPositionStop2);
        ISegment segmentStopSegment2 = segmentBuilder.createSegmentStopping(null);

        assertThat(segmentStopSegment1.getSegmentDuration()).isEqualTo(streamPositionStop1 - streamPositionStart1);
        assertThat(segmentStopSegment2.getSegmentDuration()).isEqualTo(streamPositionStop2 - streamPositionStart2);
    }

    @Test
    public void testIfViewtimeIsCalculatedCorrectlyForLiveSegmentsWithTimeshiftingOffset() throws Exception {
        int streamOffset2 = 20000;

        setStreamPosition(streamPositionStart1);
        ISegment segmentStartSegment1 = segmentBuilder.createSegmentStarting(streamOffset, streamId, null);
        setStreamPosition(streamPositionStop1);
        ISegment segmentStopSegment1 = segmentBuilder.createSegmentStopping(null);

        setStreamPosition(streamPositionStart2);
        ISegment segmentStartSegment2 = segmentBuilder.createSegmentStarting(streamOffset2, streamId, null);
        setStreamPosition(streamPositionStop2);
        ISegment segmentStopSegment2 = segmentBuilder.createSegmentStopping(null);

        assertThat(segmentStartSegment1.getSegmentDuration()).isEqualTo(0);
        assertThat(segmentStopSegment1.getSegmentDuration()).isEqualTo(streamPositionStop1 - streamPositionStart1);
        assertThat(segmentStartSegment2.getSegmentDuration()).isEqualTo(0);
        assertThat(segmentStopSegment2.getSegmentDuration()).isEqualTo(streamPositionStop2 - streamPositionStart2);
    }

    @Test
    public void testifViewpositionIsCalculatedCorrectlyForLiveSegmentsWithTimeshiftingAndWithoutOffset() throws Exception {
        int streamOffset1 = 0;
        int streamOffset2 = 20450;

        setStreamPosition(streamPositionStart1);
        ISegment segmentStartSegment1 = segmentBuilder.createSegmentStarting(streamOffset1, streamId, null);
        setStreamPosition(streamPositionStop1);
        ISegment segmentStopSegment1 = segmentBuilder.createSegmentRunning(null);
        segmentBuilder.createSegmentStopping(null);

        setStreamPosition(streamPositionStart2);
        ISegment segmentStartSegment2 = segmentBuilder.createSegmentStarting(streamOffset2, streamId, null);
        setStreamPosition(streamPositionStop2);
        ISegment segmentStopSegment2 = segmentBuilder.createSegmentRunning(null);
        setStreamPosition(streamPositionStop2 + 2000);
        segmentBuilder.createSegmentStopping(null);

        assertThat(segmentStartSegment1.getStreamPosition()).isEqualTo(streamPositionStart1 - streamOffset1);
        assertThat(segmentStopSegment1.getStreamPosition()).isEqualTo(streamPositionStop1 - streamOffset1);
        assertThat(segmentStartSegment2.getStreamPosition()).isEqualTo(streamPositionStart2 - streamOffset2);
        assertThat(segmentStopSegment2.getStreamPosition()).isEqualTo(streamPositionStop2 - streamOffset2);
    }
}
