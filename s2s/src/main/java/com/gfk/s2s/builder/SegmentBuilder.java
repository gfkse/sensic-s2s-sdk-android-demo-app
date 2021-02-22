package com.gfk.s2s.builder;

import android.util.Log;

import com.gfk.s2s.builder.segment.ISegment;
import com.gfk.s2s.builder.segment.Segment;
import com.gfk.s2s.collector.SegmentConfig;
import com.gfk.s2s.streamPositionManager.IStreamPositionCallback;
import com.gfk.s2s.utils.GlobalConst;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

public class SegmentBuilder {

    enum State {
        play, stop
    }

    enum DurationChecking {
        enabled, disabled
    }

    private Long bufferStreamPosition = null;
    private int minSegmentDuration = 0;
    private int maxStateItemsNumber = 0;
    private int maxSegmentNumber = 0;
    private State state = State.stop;
    private String presentationId = "";
    private String streamId = "";
    private int stateItemNumber = 0;
    private int segmentNumber = 0;
    private long playStreamPosition = 0;
    private int streamOffset = 0;
    private IStreamPositionCallback streamPositionCallback;
    private long startTime = 0;
    private DurationChecking durationChecking = DurationChecking.enabled;

    public SegmentBuilder(SegmentConfig segmentConfig) {

        this.minSegmentDuration = segmentConfig.getMinSegmentDuration();
        this.maxStateItemsNumber = segmentConfig.getMaxStateItemsNumber();
        this.maxSegmentNumber = segmentConfig.getMaxSegmentNumber();
    }

    public void setStreamPositionCallback(IStreamPositionCallback streamPositionCallback) {
        this.streamPositionCallback = streamPositionCallback;
    }

    public ISegment createSegmentStarting(int streamOffset, String streamId, Long bufferStreamPosition) {
        this.bufferStreamPosition = bufferStreamPosition;

        if (isMaxStateItemNumberReached()) {
            Log.e(GlobalConst.LOG_TAG, "Error in start event: Max state item number is reached. No data will be send.");
            return null;
        } else if (isMaxSegmentNumberReached()) {
            Log.e(GlobalConst.LOG_TAG, "Error in start event: Max SegmentNumber is reached. No data will be send.");
            return null;
        }

        startTime = getTimeSeconds();
        setSegmentAsPlayed();
        this.generateStreamSegment(streamOffset, streamId);
        this.incrementStateItemNumber();
        this.incrementSegmentNumber();

        return this.createSegment(0);
    }

    public ISegment createSegmentRunning(Long bufferStreamPosition) {
        this.bufferStreamPosition = bufferStreamPosition;

        if (isMaxStateItemNumberReached()) {
            Log.e(GlobalConst.LOG_TAG, "Error in run event: Max state item number is reached. No data will be send.");
            return null;
        } else if (this.isSegmentStopped()) {
            Log.e(GlobalConst.LOG_TAG, "Error in run event: Video has already stopped. No data will be send.");
            return null;
        }

        int segmentDuration = this.calculateSegmentDuration();
        if (hasMinDurationNotReached(segmentDuration)) {
            Log.e(GlobalConst.LOG_TAG, String.format("Error in run event: Duration (%d milliseconds) of video watching is too short. Player position: %d milliseconds. No data will be send.", segmentDuration, getCurrentStreamPosition()));
            return null;
        } else if (skipItemIfDurationIsInvalid(segmentDuration)) {
            Log.e(GlobalConst.LOG_TAG, String.format("Error in run event: Duration (%d milliseconds) of video watching is too short. Player position: %d milliseconds. No data will be send.", segmentDuration, getCurrentStreamPosition()));
            return null;
        }

        this.incrementStateItemNumber();

        return this.createSegment(segmentDuration);
    }

    public ISegment createSegmentStopping(Long bufferStreamPosition) {
        this.bufferStreamPosition = bufferStreamPosition;

        if (this.isSegmentStopped()) {
            Log.e(GlobalConst.LOG_TAG, "Error in stop event: Video has already stopped. No data will be send.");
            this.setSegmentAsStopped();
            return null;
        }

        int segmentDuration = this.calculateSegmentDuration();
        if (hasMinDurationNotReached(segmentDuration)) {
            this.setSegmentAsStopped();
            Log.e(GlobalConst.LOG_TAG, String.format("Error in stop event: Duration (%d milliseconds) of video watching is too short. Player position: %d milliseconds. No data will be send.", segmentDuration, getCurrentStreamPosition()));
            return null;
        } else if (skipItemIfDurationIsInvalid(segmentDuration)) {
            this.setSegmentAsStopped();
            Log.e(GlobalConst.LOG_TAG, String.format("Error in stop event: Duration (%d milliseconds) of video watching is too short. Player position: %d milliseconds. No data will be send.", segmentDuration, getCurrentStreamPosition()));
            return null;
        }

        this.setSegmentAsStopped();
        this.incrementStateItemNumber();

        return this.createSegment(segmentDuration);
    }

    public void disableDurationChecking() {
        durationChecking = DurationChecking.disabled;
    }

    private boolean skipItemIfDurationIsInvalid(int segmentDuration) {
        if (durationChecking == DurationChecking.disabled) {
            return false;
        }

        int toleranceSeconds = 3;
        long realDuration = (getTimeSeconds() - startTime + toleranceSeconds);
        int segmentDurationSeconds = (int) Math.round((double) segmentDuration / 1000);

        return realDuration < segmentDurationSeconds;
    }

    private boolean hasMinDurationNotReached(int segmentDuration) {
        return segmentDuration < minSegmentDuration;
    }

    private boolean isMaxStateItemNumberReached() {
        return maxStateItemsNumber < getStateItemNumber();
    }

    private boolean isMaxSegmentNumberReached() {
        return maxSegmentNumber < getSegmentNumber();
    }

    private long getTimeSeconds() {
        int milliseconds = 1000;
        return new Date().getTime() / milliseconds;
    }

    private void setSegmentAsPlayed() {
        this.state = State.play;
    }

    private void setSegmentAsStopped() {
        this.state = State.stop;
    }

    private boolean isSegmentStopped() {
        return this.state == State.stop;
    }

    public String getPresentationId() {
        return this.presentationId;
    }

    private int calculateSegmentDuration() {
        return (int) (getCurrentStreamPosition() - this.playStreamPosition);
    }

    private long getCurrentStreamPosition() {
        if (bufferStreamPosition == null) {
            return this.streamPositionCallback.getStreamPosition() - this.streamOffset;
        }
        return this.bufferStreamPosition - this.streamOffset;
    }

    public int getStateItemNumber() {
        return this.stateItemNumber;
    }

    public int getSegmentNumber() {
        return this.segmentNumber;
    }

    private void incrementStateItemNumber() {
        this.stateItemNumber++;
    }

    private void incrementSegmentNumber() {
        this.segmentNumber++;
    }

    private void generateStreamSegment(int streamOffset, String streamId) {

        if (!this.streamId.equals(streamId)) {
            this.streamId = streamId;
            this.presentationId = generatePresentationId(streamId);
            this.stateItemNumber = 0;
            this.segmentNumber = 0;
        }

        this.streamOffset = streamOffset;
        this.playStreamPosition = getCurrentStreamPosition();
    }

    private ISegment createSegment(int segmentDuration) {
        return new Segment(this.getStateItemNumber(), this.getSegmentNumber(), this.getCurrentStreamPosition(), this.getPresentationId(), segmentDuration);
    }

    private String generatePresentationId(String streamId) {
        int rnd = getRndInt(1000000, 9999999);
        return md5(streamId).toUpperCase() + rnd + new Date().getTime();
    }

    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private int getRndInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }
}
