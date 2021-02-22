package com.gfk.s2s.FunctionalTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

@RunWith(LibTestRunner.class)
public class LiveStreamTest {

    PlayerFunctionsSimulation simulator;
    Integer streamPos;

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
    public void test_LS1_break_without_stop_event_after_410_secs() {
        String title = "Stream, offset = 0, position = 57600000 (16:00)";

        simulator.streamStream(title, 0, 57600000);
        simulator.runningStream(10000); // pos=57610000 (16:00:10), vt=10
        simulator.isStreamPosition(57600000);
        simulator.isSegmentDuration(0);
        simulator.triggerHeartbeat();

        simulator.runningStream(20000); // pos=57630000 (16:00:30), vt=30
        simulator.triggerHeartbeat();

        simulator.runningStream(30000); // pos=57660000 (16:01:00), vt=60
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=57710000 (16:01:50), vt=110
        simulator.triggerHeartbeat();

        simulator.runningStream(10000); // pos=57720000 (16:02:40), vt=120
        simulator.changeScreen();
        simulator.isStreamPosition(57720000);
        simulator.isSegmentDuration(120000);

        simulator.runningStream(40000); // pos=57760000 (16:03:20), vt=160
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=57810000 (16:04:10), vt=210
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=57860000 (16:05:00), vt=260
        simulator.triggerHeartbeat();

        simulator.runningStream(40000); // pos=57900000 (16:05:40), vt=300
        simulator.stopStream();
        simulator.isStreamPosition(57900000);
        simulator.isSegmentDuration(300000);
    }

    @Test
    public void test_LS2_stream_with_timeshifting() {
        String title = "Stream, offset = 0, position = 57600000 (16:00)";

        simulator.streamStream(title, 0, 57600000);
        simulator.runningStream(10000); // pos=57610000 (16:00:10), vt=10
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57610000);

        simulator.runningStream(20000); // pos=57630000 (16:00:30), vt=30
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57630000);

        simulator.runningStream(30000); // pos=57660000 (16:01:00), vt=60
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57660000);

        simulator.runningStream(50000); // pos=57710000 (16:01:50), vt=110
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57710000);

        simulator.runningStream(50000); // pos=57760000 (16:02:40), vt=160
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(160000);

        simulator.runningStream(20000); // pos=57780000 (16:03:00), vt=180
        simulator.pauseLiveStream(30000, title);
        simulator.isStreamPosition(57780000); // pos=57780000 (16:03:30) shift=30
        simulator.isSegmentDuration(0);

        simulator.runningStream(10000); // pos=57790000 (16:04:10), vt=10
        simulator.triggerHeartbeat();

        simulator.runningStream(20000); // pos=57810000 (16:04:30), vt=30
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57810000);

        simulator.runningStream(30000); // pos=57840000 (16:05:00), vt=60
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57840000);

        simulator.runningStream(50000); // pos=57890000 (16:05:50), vt=110
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57890000);

        simulator.runningStream(50000); // pos=57940000 (16:06:40), vt=160
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(57940000);
        simulator.isSegmentDuration(160000);

        simulator.runningStream(20000); // pos=57960000 (16:07:00), vt=180
        simulator.stopStream();
        simulator.isStreamPosition(57960000);
        simulator.isSegmentDuration(180000);
    }
}