package com.gfk.s2s.transmitter;

import android.content.Context;

import com.gfk.s2s.builder.AllowedPlayType;
import com.gfk.s2s.builder.Builder;
import com.gfk.s2s.builder.event.EventPlay;
import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.collector.Collector;
import com.gfk.s2s.collector.Config;
import com.gfk.s2s.collector.ICollectorConfigCallback;
import com.gfk.s2s.utils.HTTPClient;
import com.gfk.s2s.utils.IHttpClientCallback;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(LibTestRunner.class)
public class ExpBackoffTransmitterTest extends TestCase {
    private Builder builder;
    private CountDownLatch signal;

    private String mediaId;
    private String contentId;
    private int streamPosition;
    private String presentationId;
    private int segmentStateItemNumber;
    private int segmentNumber;
    private int segmentDuration;
    private HashMap<String, String> options = new HashMap<String, String>(){{put("screen", "fullscreen"); put("volume", "mute");put("deviceType", "TV");}};
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
    private HashMap customParameters;

    private @Mock HTTPClient.Instantiable mockedClient;
    private @Mock ScheduledExecutorService mockedExecutorService;
    private ExpBackoffTransmitter transmitter;
    private boolean httpRequestSuccess = true;
    private LoopCaptor loopCaptor;

    @Before
    public void setup() throws InterruptedException {
        Context context = RuntimeEnvironment.application;
        Collector collector = new Collector(context);
        signal = new CountDownLatch(1);

        mediaId = "mediaId";
        contentId = "contentId";
        streamPosition = 0;
        presentationId = "1234";
        segmentStateItemNumber = 1;
        segmentNumber = 1;
        segmentDuration = 120;
        customParameters = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "cba");}};

        String unitTestConfigUrl = "http://localhost:8881/s2s-android-unittest-500.json";
        collector.loadConfig(unitTestConfigUrl, new ICollectorConfigCallback() {
            @Override
            public void onCompletion(Config config, boolean success) {
                signal.countDown();
            }

            @Override
            public void onFinished() {
                signal.countDown();
            }
        });
        signal.await();

        MockitoAnnotations.initMocks(this);
        this.builder = new Builder("myMediaID", collector);
        transmitter = new ExpBackoffTransmitter(collector.getTrackingUrl());
        transmitter.setHttpClient(mockedClient);
        transmitter.setExecutorService(mockedExecutorService);
        httpRequestSuccess = true;
        doAnswer(invocation -> {
            IHttpClientCallback cb = (IHttpClientCallback) invocation.getArguments()[2];
            cb.onCompletion(httpRequestSuccess);
            return null;
        }).when(mockedClient).post(anyString(), anyString(), any(IHttpClientCallback.class));
        loopCaptor = new LoopCaptor();
        doAnswer(loopCaptor).when(mockedExecutorService).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    public void test_ifTransmittingSucceedsImmediately_requestShouldNotBeStored() {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = true;
        transmitter.sendRequest(request);

        verify(mockedClient).post(anyString(), anyString(), any());
        assertEquals(0, transmitter.storage.size());
    }

    @Test
    public void test_ifTransmittingInitiallyFails_requestShouldBeStored() {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = false;
        transmitter.sendRequest(request);

        assertEquals(1, transmitter.storage.size());
    }

    @Test
    public void test_ifTransmittingInitiallyFails_loopShouldBeScheduled() {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = false;
        transmitter.sendRequest(request);

        verify(mockedExecutorService, times(1)).schedule(any(Runnable.class), anyLong(), any());
        assertEquals(1, loopCaptor.loops.size());
    }

    @Test
    public void test_ifLoopIsScheduled_shouldResendAllRequests() throws InterruptedException, ExecutionException {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = false;

        transmitter.sendRequest(request);
        transmitter.sendRequest(request);
        transmitter.sendRequest(request);

        verify(mockedClient, times(3)).post(anyString(), anyString(), any());

        httpRequestSuccess = true;

        loopCaptor.advance().get();
        verify(mockedClient, times(6)).post(anyString(), anyString(), any());
        assertEquals(0, loopCaptor.loops.size());
    }

    @Test
    public void test_ifLoopIsScheduled_andResendingFails_anotherLoopShouldBeScheduled() throws InterruptedException, ExecutionException {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = false;

        transmitter.sendRequest(request);
        transmitter.sendRequest(request);
        transmitter.sendRequest(request);

        verify(mockedClient, times(3)).post(anyString(), anyString(), any());

        loopCaptor.advance().get();

        verify(mockedClient, times(4)).post(anyString(), anyString(), any());

        loopCaptor.advance().get();

        verify(mockedClient, times(5)).post(anyString(), anyString(), any());
        assertEquals(1, loopCaptor.loops.size());
    }

    @Test
    public void test_ifLoopIsActive_noNewLoopShouldBeScheduled() {
        EventPlay event = new EventPlay(mediaId, contentId, streamPosition, "", 20, 0L, presentationId, segmentNumber, segmentStateItemNumber, segmentDuration, AllowedPlayType.live, playOptions, customParameters);
        IRequest request = builder.getRequestBuilder().buildRequest(event);

        httpRequestSuccess = false;
        transmitter.sendRequest(request);
        transmitter.sendRequest(request);
        transmitter.sendRequest(request);

        verify(mockedExecutorService, times(1)).schedule(any(Runnable.class), anyLong(), any());
        assertEquals(1, loopCaptor.loops.size());
    }


    private static class LoopCaptor implements Answer<Runnable> {

        static ExecutorService executor = Executors.newSingleThreadExecutor();
        Queue<Runnable> loops = new LinkedList<>();

        @Override
        public Runnable answer(InvocationOnMock invocation) {
            loops.add((Runnable) invocation.getArguments()[0]);
            return null;
        }

        Future advance() {
            return executor.submit(loops.remove());
        }
    }

}
