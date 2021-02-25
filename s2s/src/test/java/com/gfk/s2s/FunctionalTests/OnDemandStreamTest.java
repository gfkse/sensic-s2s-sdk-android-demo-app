package com.gfk.s2s.FunctionalTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

@RunWith(LibTestRunner.class)
public class OnDemandStreamTest {

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
    public void test_VOD_1_Break_without_stop_event_on_second_segment_after_270_secs() {
        String title = "Stream, offset = 0, position = 0";
        streamPos = 0;

        simulator.streamStream(title, 0 ,null);
        simulator.runningStream(10000); // pos=10, vt=10
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(10000);

        simulator.runningStream(20000); // pos=30, vt=30
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(30000);

        simulator.runningStream(30000); // pos=60, vt=60
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(60000);

        simulator.runningStream(50000); // pos=110, vt=110
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(110000);

        simulator.runningStream(50000); // pos=160, vt=160
        simulator.triggerHeartbeat();
        simulator.isStreamPosition(160000);

        simulator.runningStream(20000); // pos=180, vt=180
        simulator.stopStream();
        simulator.isStreamPosition(180000);
        simulator.isSegmentDuration(180000);

        simulator.streamStream(title, 0, null);
        simulator.runningStream(10000); // pos=190, vt=10
        simulator.triggerHeartbeat();

        simulator.runningStream(20000); // pos=210, vt=30
        simulator.triggerHeartbeat();

        simulator.runningStream(30000); // pos=240, vt=60
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=290, vt=110
        simulator.triggerHeartbeat();

        simulator.runningStream(10000); // pos=300, vt=120
        simulator.changeScreen();
        simulator.isSegmentDuration(120000);
        simulator.isStreamPosition(300000);

        simulator.runningStream(40000); // pos=340, vt=160
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=390, vt=210
        simulator.triggerHeartbeat();

        simulator.runningStream(50000); // pos=440, vt=260
        simulator.triggerHeartbeat();

        simulator.runningStream(10000); // pos=450, vt=270
        simulator.isStreamPosition(440000);
        simulator.isSegmentDuration(260000);
    }

    @Test
    public void test_VOD_2_Break_with_next_stream_by_second_segment_after_20_secs() {
        String title = "Stream, offset = 0, position = 0";
        streamPos = 0;

        simulator.streamStream(title, 0, null);
        simulator.runningStream(10000); // pos=10 , vt=10
        simulator.triggerHeartbeat();
        simulator.runningStream(10000); // pos=20 , vt=20

        simulator.seekForwardStream(300000, title); // pos=300, vt=0
        simulator.isSegmentDuration(0);
        simulator.isStreamPosition(320000);

        simulator.runningStream(10000); // pos=330000, vt=10
        simulator.triggerHeartbeat();
        simulator.runningStream(20000); // pos=350000, vt=30
        simulator.triggerHeartbeat();
        simulator.runningStream(30000); // pos=380000, vt=60
        simulator.changeVolume();

        simulator.runningStream(50000); // pos=430000, vt=110
        simulator.triggerHeartbeat();
        simulator.runningStream(50000); // pos=480000, vt=160
        simulator.triggerHeartbeat();
        simulator.runningStream(10000); // pos=490000, vt=170
        simulator.stopStream();
        simulator.isStreamPosition(490000);
        simulator.isSegmentDuration(170000);
    }

    @Test
    public void test_VOD_3_Break_with_next_stream_by_second_segment_after_20_secs() {
        String title = "Stream, offset = 0, position = 0";
        streamPos = 0;

        simulator.streamStream(title, 0, null);
        simulator.runningStream(2000); // pos=2 , vt=2

        simulator.seekForwardStream(180000, title); // pos=182, vt=0
        simulator.isSegmentDuration(0);
        simulator.isStreamPosition(182000);

        simulator.seekForwardStream(120000, title); // pos=302, vt=0
        simulator.runningStream(20000); // pos=322, vt=20
        simulator.stopStream();
        simulator.isStreamPosition(322000);
        simulator.isSegmentDuration(20000);

        simulator.streamStream("other stream", 0, null);
    }

    @Test
    public void test_VOD_4_Seek_Forward_and_break_with_closing_Browser_without_StopEvent() {
        String title = "Stream, offset = 0, position = 0";
        streamPos = 0;

        simulator.streamStream(title, 0, null);
        simulator.seekForwardStream(130000, title); // pos=130, vt=0
        simulator.isSegmentDuration(0);
        simulator.isStreamPosition(130000);

        simulator.runningStream(10000); // pos=140, vt=10
        simulator.triggerHeartbeat();
        simulator.runningStream(20000); // pos=160, vt=30
        simulator.triggerHeartbeat();
        simulator.runningStream(30000); // pos=190, vt=60
        simulator.triggerHeartbeat();
        simulator.isSegmentDuration(60000);
        simulator.isStreamPosition(190000);

        simulator.seekBackwardStream(10000, title); // pos=180, vt=0
        simulator.isSegmentDuration(0);
        simulator.isStreamPosition(180000);

        simulator.runningStream(10000); // pos=190, vt=10
        simulator.triggerHeartbeat();
        simulator.runningStream(20000); // pos=210, vt=30
        simulator.triggerHeartbeat();
        simulator.runningStream(30000); // pos=240, vt=60
        simulator.triggerHeartbeat();
        simulator.runningStream(50000); // pos=290, vt=110
        simulator.triggerHeartbeat();
        simulator.runningStream(10000); // pos=300, vt=120
        simulator.isStreamPosition(290000);
        simulator.isSegmentDuration(110000);
    }
}