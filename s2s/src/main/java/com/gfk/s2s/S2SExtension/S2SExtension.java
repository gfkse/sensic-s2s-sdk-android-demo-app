package com.gfk.s2s.S2SExtension;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gfk.s2s.s2sagent.S2SAgent;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import java.util.HashMap;


public class S2SExtension {

    private ExoPlayer exoPlayer;
    private double lastPlayPosition = 0L;
    private SensicEvent lastEvent = SensicEvent.unknown;
    private String contentId;
    private HashMap<String, Object> customParams;
    private S2SAgent s2sAgent;
    private double currentPlayPositionSeconds;
    private boolean lastPlayerStatusWasPlaying;
    private String videoUrl;
    private Handler timeObserver;

    public S2SExtension(@NonNull String contentId, @NonNull String videoUrl, @Nullable HashMap<String, Object> customParams) {
        this.contentId = contentId;
        this.customParams = customParams;
        this.videoUrl = videoUrl;
    }

    public void setParameters(@NonNull String contentId, @NonNull String videoUrl, @Nullable HashMap<String, Object> customParams) {
        this.contentId = contentId;
        this.customParams = customParams;
        this.videoUrl = videoUrl;
    }

    public void bindPlayer(@NonNull S2SAgent agent, @NonNull ExoPlayer player) {
        this.exoPlayer = player;
        this.s2sAgent = agent;
        stopTimeObserver();
        startTimeObserver();
        observeTime();

        if (exoPlayer.getPlaybackState() != Player.STATE_BUFFERING && exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
            if (exoPlayer.isPlaying()) {
                lastEvent = SensicEvent.play;
                lastPlayerStatusWasPlaying = true;
            } else {
                lastEvent = SensicEvent.stop;
            }
        }

        this.s2sAgent.setStreamPositionCallback(() -> player.getCurrentPosition() > 1000 ? (int) player.getCurrentPosition() : 0);
    }

    private void observeTime() {
        long intervalMS = 500L;

        timeObserver.postDelayed(() -> {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING || exoPlayer.getPlaybackState() == Player.STATE_IDLE) {
                observeTime();
                return;
            }

            currentPlayPositionSeconds = Math.max(exoPlayer.getCurrentPosition(), 0L) / 1000.0;
            boolean hugeTimeJump = Math.abs((currentPlayPositionSeconds - lastPlayPosition)) >= (intervalMS / 1000.0 * 3.0);

            if (hugeTimeJump && lastEvent == SensicEvent.play) {
                s2sAgent.stop((long) (lastPlayPosition * 1000L));
                lastEvent = SensicEvent.timeJump;
            } else if (lastEvent == SensicEvent.play && exoPlayer.getDuration() / 1000.0 <= currentPlayPositionSeconds) {
                s2sAgent.stop(null);
                lastEvent = SensicEvent.stop;
            } else if (lastEvent == SensicEvent.timeJump) {
                s2sAgent.playStreamOnDemand(contentId, videoUrl, customParams);
                lastEvent = SensicEvent.play;
            } else if (lastPlayerStatusWasPlaying != exoPlayer.isPlaying()) {
                if (lastEvent == SensicEvent.play) {
                    s2sAgent.stop(null);
                    lastEvent = SensicEvent.stop;
                } else {
                    s2sAgent.playStreamOnDemand(contentId, videoUrl, customParams);
                    lastEvent = SensicEvent.play;
                }
            }

            lastPlayPosition = currentPlayPositionSeconds;
            lastPlayerStatusWasPlaying = exoPlayer.isPlaying();
            observeTime();

        }, intervalMS);
    }

    private void stopTimeObserver() {
        if (timeObserver != null) {
            timeObserver.removeMessages(0);
        }
    }

    private void startTimeObserver() {
        timeObserver = new Handler(exoPlayer.getApplicationLooper());
        timeObserver.postDelayed(this::observeTime, 0);

    }


}