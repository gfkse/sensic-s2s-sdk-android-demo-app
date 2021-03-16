package com.gfk.s2s.sensic_demo_app_android;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gfk.s2s.s2sagent.S2SAgent;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;

import java.util.HashMap;


public class S2SExtension {

    private ExoPlayer exoPlayer;
    private String contentId;
    private HashMap<String, Object> customParams;
    private S2SAgent s2sAgent;
    private String videoUrl;
    private Handler timeObserver;
    private final Long timerIntervallMS = 500L;
    private double currentPlayPositionSeconds;
    private double lastPlayPositionSeconds = 0L;
    private SensicEvent lastEvent = SensicEvent.unknown;
    private boolean lastPlayerStatusWasPlaying;
    private Timeline.Period period;
    private long firstStartLiveWithPositionMS = 0;

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

    private void setupAgentAndPlayer(@NonNull S2SAgent agent, @NonNull ExoPlayer player) {
        this.exoPlayer = player;
        this.s2sAgent = agent;

        if (exoPlayer.getPlaybackState() != Player.STATE_BUFFERING && exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
            if (exoPlayer.isPlaying()) {
                lastEvent = SensicEvent.play;
                lastPlayerStatusWasPlaying = true;
            } else {
                lastEvent = SensicEvent.stop;
            }
        }
    }

    public void bindPlayer(@NonNull S2SAgent agent, @NonNull ExoPlayer player) {
        setupAgentAndPlayer(agent, player);
        this.s2sAgent.setStreamPositionCallback(() -> player.getCurrentPosition() > 1000 ? (int) player.getCurrentPosition() : 0);
        if (timeObserver != null) {
            timeObserver.removeMessages(0);
        }
        timeObserver = new Handler(exoPlayer.getApplicationLooper());
        observeTime();
    }

    public void bindLivePlayer(@NonNull S2SAgent agent, @NonNull ExoPlayer player) {
        setupAgentAndPlayer(agent, player);

        this.s2sAgent.setStreamPositionCallback(() -> {
            long position = getLiveStreamPositionMS();
            return position > 1000 ? (int) position : 0;
        });
        if (timeObserver != null) {
            timeObserver.removeMessages(0);
        }
        timeObserver = new Handler(exoPlayer.getApplicationLooper());
        observeTimeLive();
    }

    private boolean isHugeTimeJump() {
        return Math.abs((currentPlayPositionSeconds - lastPlayPositionSeconds)) >= (timerIntervallMS / 1000.0 * 3.0);
    }

    private void observeTime() {

        timeObserver.postDelayed(() -> {
            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                observeTime();
                return;
            }
            if (exoPlayer.getPlaybackState() == Player.STATE_IDLE) {
                unbindPlayer();
                return;
            }

            currentPlayPositionSeconds = Math.max(exoPlayer.getCurrentPosition(), 0L) / 1000.0;

            if (isHugeTimeJump() && lastEvent == SensicEvent.play) {
                s2sAgent.stop((long) (lastPlayPositionSeconds * 1000L));
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

            lastPlayPositionSeconds = currentPlayPositionSeconds;
            lastPlayerStatusWasPlaying = exoPlayer.isPlaying();
            observeTime();

        }, timerIntervallMS);
    }


    private void observeTimeLive() {
        timeObserver.postDelayed(() -> {

            if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                observeTimeLive();
                return;
            }
            if (exoPlayer.getPlaybackState() == Player.STATE_IDLE) {
                unbindPlayer();
                return;
            }

            if (firstStartLiveWithPositionMS == 0) {
                firstStartLiveWithPositionMS = System.currentTimeMillis() - getLiveStreamPositionMS();
            }
            currentPlayPositionSeconds = getLiveStreamPositionMS() / 1000.0;

            if (isHugeTimeJump() && lastEvent == SensicEvent.play) {
                timeObserver.post(() -> s2sAgent.stop());
                lastEvent = SensicEvent.timeJump;

            } else if (lastEvent == SensicEvent.timeJump) {
                long offset = getCurrentLiveOffsetMs();
                timeObserver.post(() -> s2sAgent.playStreamLive(contentId, "", (int) offset, videoUrl, customParams));
                lastEvent = SensicEvent.play;
            } else if (lastPlayerStatusWasPlaying != exoPlayer.isPlaying()) {

                if (lastEvent != SensicEvent.play) {
                    long offset = getCurrentLiveOffsetMs();
                    timeObserver.post(() -> s2sAgent.playStreamLive(contentId, "", (int) offset, videoUrl, customParams));
                    lastEvent = SensicEvent.play;
                } else {
                    timeObserver.post(() -> s2sAgent.stop());
                    lastEvent = SensicEvent.stop;
                }
            }

            lastPlayPositionSeconds = getLiveStreamPositionMS() / 1000.0;
            lastPlayerStatusWasPlaying = exoPlayer.isPlaying();
            observeTimeLive();

        }, timerIntervallMS);

    }

    private long getCurrentLiveOffsetMs() {
        long value = 0;

        Timeline timeline = exoPlayer.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return value;
        }
        long timeDifference = System.currentTimeMillis() - firstStartLiveWithPositionMS;
        long streamEstimatedTimeMS = firstStartLiveWithPositionMS + timeDifference;
        long playerTime = firstStartLiveWithPositionMS + getLiveStreamPositionMS();
        value = streamEstimatedTimeMS - playerTime;
        value *= -1;
        return value;
    }

    private long getLiveStreamPositionMS() {
        if (period == null) {
            period = new Timeline.Period();
        }
        long position = exoPlayer.getCurrentPosition();
        Timeline currentTimeline = exoPlayer.getCurrentTimeline();
        if (!currentTimeline.isEmpty()) {
            position -= currentTimeline.getPeriod(exoPlayer.getCurrentPeriodIndex(), period)
                    .getPositionInWindowMs();
        }
        return position;

    }

    private void unbindPlayer() {
        Log.i("GFK S2S SDK", "Exoplayer not set");
        timeObserver.removeMessages(0);
        exoPlayer = null;
    }
}