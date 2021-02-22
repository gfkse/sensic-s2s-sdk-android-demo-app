package com.gfk.s2s.transmitter;

import android.util.Log;

import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.utils.GlobalConst;
import com.gfk.s2s.utils.HTTPClient;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpBackoffTransmitter implements ITransmitter {

    private static int BASE_DELAY = 500;
    private static int MAX_DELAY = 32000;

    private class Loop implements Runnable {
        @Override
        public void run() {
            Iterator<IRequest> reqIt = storage.listIterator();
            AtomicBoolean allEventsSent = new AtomicBoolean(true);
            while (reqIt.hasNext()) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                String requestString = reqIt.next().getAsUrlString();
                httpClient.post(trackingUrl, requestString, success -> {
                    if (success) {
                        Log.d("GfKlog", "resent request: " + requestString);
                        reqIt.remove();
                    } else {
                        allEventsSent.set(false);
                    }
                    countDownLatch.countDown();
                });
                if (!allEventsSent.get()) {
                    break;
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Log.e(GlobalConst.LOG_TAG, "Exception in ScheduleLoop with message: " + e.getMessage());
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }
            if (allEventsSent.get()) {
                isLoopActive.set(false);
                currentDelay = 0;
            } else {
                increaseDelay();
                scheduleLoop();
            }
        }
    }

    private ScheduledExecutorService loopExecutorService = Executors.newScheduledThreadPool(1);
    private HTTPClient.Instantiable httpClient = new HTTPClient.Instantiable();
    private int currentDelay = 0;
    private final String trackingUrl;
    LinkedList<IRequest> storage;
    private AtomicBoolean isLoopActive = new AtomicBoolean(false);

    void setHttpClient(HTTPClient.Instantiable httpClient) {
        this.httpClient = httpClient;
    }
    void setExecutorService(ScheduledExecutorService executorService) { this.loopExecutorService = executorService; }

    public ExpBackoffTransmitter(String trackingUrl) {
        this.trackingUrl = trackingUrl;

        storage = new LinkedList<>();
    }

    @Override
    public void sendRequest(IRequest request) {
        final String postParams = request.getAsUrlString();

        httpClient.post(trackingUrl, postParams, success -> {
            if (success) {
                Log.d("GfKlog", "sending request: " + request.getAsUrlString());
            } else {
                Log.d("GfKlog", "failed to send request: " + request.getAsUrlString());
                storage.add(request);
                if (!isLoopActive.get()) {
                    isLoopActive.set(true);
                    scheduleLoop();
                }
            }
        });
    }

    private void increaseDelay() {
        if (currentDelay == 0) {
            currentDelay = ExpBackoffTransmitter.BASE_DELAY;
        } else {
            currentDelay = Math.min(currentDelay * 2, ExpBackoffTransmitter.MAX_DELAY);
        }
        Log.d("GfKlog", "Current delay for sending in the loop: " + currentDelay);
    }

    private void scheduleLoop() {
        loopExecutorService.schedule(new Loop(), currentDelay, TimeUnit.MILLISECONDS);
    }
}
