package com.gfk.s2s.registration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gfk.s2s.s2sagent.S2SAgent;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import java.util.HashMap;

enum SensicEvent {
    play, stop, unknown
}

public class S2SExtension {

    private ExoPlayer exoPlayer;
    private SensicEvent lastEvent = SensicEvent.unknown;
    private String contentId;
    private HashMap<String, Object> customParams;
    private S2SAgent s2sAgent;
    private String videoUrl;
    private boolean hugeJump = false;

    public S2SExtension(@NonNull String contentId, @NonNull String videoUrl, @Nullable HashMap<String, Object> customParams) {
        this.contentId = contentId;
        this.customParams = customParams;
        this.videoUrl = videoUrl;
    }

    public void setParameter(@NonNull String contentId, @NonNull String videoUrl, @Nullable HashMap<String, Object> customParams) {
        this.contentId = contentId;
        this.customParams = customParams;
        this.videoUrl = videoUrl;
    }

    public void bindPlayer(@NonNull S2SAgent agent, @NonNull ExoPlayer player) {
        this.exoPlayer = player;
        this.s2sAgent = agent;
        this.s2sAgent.setStreamPositionCallback(() -> player.getCurrentPosition() > 1000 ? (int) player.getCurrentPosition() : 0);
        exoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onPositionDiscontinuity(int reason) {
                if (reason == ExoPlayer.DISCONTINUITY_REASON_SEEK || reason == ExoPlayer.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                    hugeJump = true;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying && lastEvent == SensicEvent.play) {
                    agent.stop(null);
                    lastEvent = SensicEvent.stop;
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_IDLE:
                        // not able to play
                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        // not enough data. also every seek
                        break;
                    case ExoPlayer.STATE_ENDED:
                        // called once at the end of the video or after skipping (STATE_BUFFERING -> STATE_ENDED -> STATE_READY)
                        // we use onIsPlayingChanged for this case
                        break;

                    case ExoPlayer.STATE_READY:
                        if (hugeJump) {
                            if (lastEvent == SensicEvent.stop) {
                                agent.playStreamOnDemand(contentId, videoUrl, customParams);
                            }
                            agent.stop(exoPlayer.getCurrentPosition());
                            lastEvent = SensicEvent.stop;
                            hugeJump = false;
                        }

                        if (exoPlayer.isPlaying()) {
                            if (lastEvent != SensicEvent.play) {
                                s2sAgent.playStreamOnDemand(contentId, videoUrl, customParams);
                                lastEvent = SensicEvent.play;
                            }
                        } else {
                            if (lastEvent != SensicEvent.stop) {
                                s2sAgent.stop(null);
                                lastEvent = SensicEvent.stop;
                            }
                        }
                        break;
                }
            }
        });
    }

}