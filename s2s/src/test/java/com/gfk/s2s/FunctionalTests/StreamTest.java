package com.gfk.s2s.FunctionalTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

@RunWith(LibTestRunner.class)
public class StreamTest {

    PlayerFunctionsSimulation simulator;
    Integer streamPos;
    private int result;

    @Before
    public void setUp() {
        simulator = new PlayerFunctionsSimulation(new PlayerFunctionsCallbacks() {

            @Override
            public Integer getStreamPosition() {
                return streamPos;
            }

            @Override
            public void setStreamPosition(Integer value) {
                streamPos = value;
            }
        });

        streamPos = 0;
    }

    @Test
    public void test_if_segments_with_offset_0_and_position_0_can_be_created_for_same_stream() {
        String title = "Stream, offset = 0, position = 0";
        simulator.streamStream(title, 0, null);
        simulator.runningStream(2000);
        simulator.seekForwardStream(20000, title);
        simulator.runningStream(2000);
        simulator.changeScreen();
        simulator.runningStream(10000);
        simulator.changeVolume();
        simulator.runningStream(15800);
        simulator.seekBackwardStream(14000, title);
        simulator.runningStream(34200);
        simulator.stopStream();
    }

    @Test
    public void test_if_segments_with_offset_20000_and_position_0_can_be_created_for_same_stream(){
        String title = "Stream, offset = 20sec, position = 0";
        simulator.streamStream(title, 20000, null);
        simulator.runningStream(2000);
        simulator.seekForwardStream(20000, title);
        simulator.runningStream(2000);
        simulator.changeScreen();
        simulator.runningStream(10000);
        simulator.changeVolume();
        simulator.runningStream(15800);
        simulator.seekBackwardStream(14000, title);
        simulator.runningStream(34200);
        simulator.stopStream();
    }

    @Test
    public void test_if_segments_with_offset_20000_and_position_12000_can_be_created_for_same_stream() {
        String title = "Stream, offset = 20sec, position = 12000";
        simulator.streamStream(title, 20000, 12000);
        simulator.runningStream(2000);
        simulator.seekForwardStream(20000, title);
        simulator.runningStream(2000);
        simulator.changeScreen();
        simulator.runningStream(10000);
        simulator.triggerVisibility();
        simulator.runningStream(15800);
        simulator.seekBackwardStream(14000, title);
        simulator.runningStream(34200);
        simulator.stopStream();
    }

    @Test
    public void test_if_segments_with_offset_0_and_position_12000_can_be_created_for_same_stream() {
        String title = "Stream, offset = 0sec, position = 12000";
        simulator.streamStream(title, 0, 12000);
        simulator.runningStream(2000);
        simulator.seekForwardStream(20000, title);
        simulator.runningStream(2000);
        simulator.changeScreen();
        simulator.runningStream(10000);
        simulator.changeVolume();
        simulator.runningStream(15800);
        simulator.seekBackwardStream(14000, title);
        simulator.runningStream(34200);
        simulator.stopStream();
    }

    @Test
    public void test_if_a_segment_have_correct_viewtime_and_position_for_offset_0() {
        String title = "Stream, offset = 0sec, position = 0";
        Integer position = 0;
        streamPos = position;

        simulator.streamStream(title, 0, streamPos);
        simulator.runningStream(10000);
        simulator.isSegmentDuration(0);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(10000);
        result = position + 10000;
        simulator.isStreamPosition(result);
        simulator.runningStream(2000);

        simulator.seekForwardStream(20000, title);
        simulator.isSegmentDuration(0);
        result = position + 10000 + 20000 + 2000;
        simulator.isStreamPosition(result);
        simulator.runningStream(10000);
        simulator.isSegmentDuration(0);
        simulator.changeScreen();
        simulator.isSegmentDuration(10000);
        simulator.runningStream(15800);
        simulator.isSegmentDuration(10000);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(25800);
        result = position + 10000 + 20000 + 25800 + 2000;
        simulator.isStreamPosition(result);

        simulator.runningStream(2000);
        simulator.seekBackwardStream(14000, title);
        result = position + 10000 + 20000 + 25800 - 14000 + 2000 + 2000;
        simulator.isStreamPosition(result);
        simulator.isSegmentDuration(0);
        simulator.runningStream(34200);
        simulator.isSegmentDuration(0);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(34200);
        result = position + 10000 + 20000 + 25800 - 14000 + 34200 + 2000 + 2000;
        simulator.isStreamPosition(result);

        simulator.runningStream(2000);
        simulator.stopStream();
        simulator.isSegmentDuration(36200);
        result = position + 10000 + 20000 + 25800 - 14000 + 34200 + 2000 + 2000 + 2000;
        simulator.isStreamPosition(result);
    }

    @Test
    public void test_if_a_segment_have_correct_viewtime_and_position_with_different_offset() {
        String title = "Stream, offset = variable, position = 12045600";
        Integer position = 12045600;
        streamPos = position;

        simulator.streamStream(title, 25200, streamPos);
        simulator.runningStream(10000);
        simulator.isSegmentDuration(0);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(10000);
        simulator.isStreamPosition(position + 10000 - 25200);

        simulator.runningStream(2000);
        simulator.seekForwardStream(20000, title);
        simulator.isSegmentDuration(0);
        result = position + 10000 + 20000 - 25200 + 2000;
        simulator.isStreamPosition(result);
        simulator.runningStream(10000);
        simulator.isSegmentDuration(0);
        simulator.changeScreen();
        simulator.isSegmentDuration(10000);
        simulator.runningStream(15800);
        simulator.isSegmentDuration(10000);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(25800);
        result = position + 10000 + 20000 + 25800 - 25200 + 2000;
        simulator.isStreamPosition(result);

        simulator.runningStream(2000);
        simulator.seekBackwardStream(14000, title);
        result = position + 10000 + 20000 + 25800 - 14000 - 25200 + 2000 + 2000;
        simulator.isStreamPosition(result);
        simulator.isSegmentDuration(0);
        simulator.runningStream(34200);
        simulator.isSegmentDuration(0);
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(34200);
        result = position + 10000 + 20000 + 25800 - 14000 + 34200 - 25200 + 2000 + 2000;
        simulator.isStreamPosition(result);

        simulator.runningStream(2000);
        simulator.pauseLiveStream(30000, title);
        simulator.runningStream(40000);

        simulator.stopStream();
        simulator.isSegmentDuration(40000);
        result = position + 10000 + 20000 + 25800 - 14000 + 34200 - 25200 + 40000 + 2000 + 2000 + 2000;
        simulator.isStreamPosition(result);
    }
}