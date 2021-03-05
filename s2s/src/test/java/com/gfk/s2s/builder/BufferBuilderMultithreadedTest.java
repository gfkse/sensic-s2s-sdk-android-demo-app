package com.gfk.s2s.builder;

import com.gfk.s2s.builder.buffer.BufferCommon;
import com.gfk.s2s.builder.buffer.BufferPlay;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.processor.Processor;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BufferBuilderMultithreadedTest extends MultithreadedTestCase {

    private Map customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "qwe");}};
    private Map<String, String> options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "100"); put("deviceType", "TV");}};
    private IEventPlayOptions playOptions = new IEventPlayOptions() {
        @Override
        public String getVolume() {
            return options.get("volume");
        }

        @Override
        public String getScreen() {
            return options.get("screen");
        }

        @Override
        public String getDeviceType() { return options.get("deviceType"); }
    };
    private BufferBuilder builder;
    Processor mockProcessor;
    private long streamPosition;
    LinkedList<BufferCommon> bufferStorage;

    @Override
    public void initialize() {
        builder = new BufferBuilder();
        builder.setStreamPositionCallback(() -> streamPosition);
        mockProcessor = mock(Processor.class);
        streamPosition = 0;
        bufferStorage = new LinkedList<>();
        streamPosition = 100;
        BufferPlay bufferPlay = builder.buildBufferPlay("c1","s1", 0, playOptions, customParameters, "", 2000, AllowedPlayType.live);
        bufferStorage.add(bufferPlay);
        BufferPlay bufferPlay2 = builder.buildBufferPlay("c2","s2", 0, playOptions, customParameters, "", 2000, AllowedPlayType.live);
        bufferStorage.add(bufferPlay2);
    }

    public void thread1() throws InterruptedException {
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
    }

    public void thread2() throws InterruptedException {
        builder.mergeBufferEvents(bufferStorage, mockProcessor);
    }

    @Override
    public void finish() {
        assertTrue(bufferStorage.isEmpty());
    }

    @Test
    public void testCounter() throws Throwable {
        TestFramework.runManyTimes(new BufferBuilderMultithreadedTest(), 100);
    }
}
