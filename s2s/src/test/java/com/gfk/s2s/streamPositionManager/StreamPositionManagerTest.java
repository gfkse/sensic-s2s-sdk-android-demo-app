package com.gfk.s2s.streamPositionManager;

import android.content.Context;

import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.Config;
import com.gfk.s2s.collector.ICollectorConfigCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@RunWith(LibTestRunner.class)
public class StreamPositionManagerTest extends TestCase {
    private Collector collector;
    private StreamPositionManager streamManager;
    private long position;

    private final String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest.json";

    @Before
    public void setup() {
        Context context = RuntimeEnvironment.application;
        this.collector = new Collector(context);
        this.streamManager = new StreamPositionManager(this.collector);
    }

    @Test
    public void test_valid_ts() throws InterruptedException {
        this.collector.loadConfig(unitTestConfigUrl, new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                IStreamPositionCallback newCallback = streamManager.getExtendedStreamPositionCallbackLive();
                position = newCallback.getStreamPosition() / 1000;
            }

            @Override
            public void onFinished() {

            }
        });

        //Thread.sleep(500);

        long val = collector.getTimeWithOffset() / 1000;

        // cause of the delay in a http request we losing some millis, allowed deviation 1 seconds (1f)
        assertThat((float)position).isCloseTo(val, offset(1f));
    }

    @Test
    public void test_invalid_ts() throws InterruptedException {
        this.collector.loadConfig("http://127.0.0.1:8881/s2s-android-unittest-fake-ts.json", new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                IStreamPositionCallback newCallback = streamManager.getExtendedStreamPositionCallbackLive();
                position = newCallback.getStreamPosition();
            }

            @Override
            public void onFinished() {

            }
        });

        //Thread.sleep(500);

        // 2 minutes = 120 seconds in milliseconds
        // the mock server returns a timestamp 2 minutes less from now

        long time = collector.getUTCTimestamp();
        long offset = (position - time) / 1000;
        long timeOffset = collector.getTimeOffset() / 1000;
        long pos = position / 1000;
        long timeWithOffset = collector.getTimeWithOffset() / 1000;
        //System.out.print("timeWithOffset=" + timeWithOffset+"\n");
        //System.out.print("pos=" + pos);

        // cause of the delay in a http request we losing some millis, allowed deviation 1 seconds (1f)
        assertThat((float)offset).isCloseTo(timeOffset, offset(1f));
        assertThat((float)pos).isCloseTo(timeWithOffset, offset(1f));
    }
}
