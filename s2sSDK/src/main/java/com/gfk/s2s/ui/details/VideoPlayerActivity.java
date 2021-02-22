package com.gfk.s2s.ui.details;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.gfk.s2s.Endpoint;
import com.gfk.s2s.cast.ExpandedControlsActivity;
import com.gfk.s2s.demo.s2s.R;
import com.gfk.s2s.s2sagent.S2SAgent;
import com.gfk.s2s.ui.EndpointHelper;
import com.gfk.s2s.ui.videolist.ContentAdapter;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.util.HashMap;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_BUFFERING;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_IDLE;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PAUSED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;


public class VideoPlayerActivity extends BasePlayerActivity {

    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    public enum PlaybackState {
        PLAYING, PAUSED, IDLE
    }

    private long lastKnownRemoteStreamPosition = 0;
    private CastContext castContext;
    private PlaybackLocation playbackLocation;
    private SessionManagerListener<CastSession> sessionManagerListener;
    private RemoteMediaClient.Callback mediaClientCallback;
    private String deviceType = "";
    private CastSession currentCastSession;
    private PlaybackState playbackState;
    private int remoteMediaStatus = PLAYER_STATE_IDLE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        castContext = CastContext.getSharedInstance(this);
        currentCastSession = castContext.getSessionManager().getCurrentCastSession();
        setupCastListener();
        addSessionsManagerListener();
        Context context = getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String endpointUrl = EndpointHelper.getEndpointUrl(context);

        agent = new S2SAgent(endpointUrl, "s2sdemomediaid_ssa_android", preferences.getBoolean("optin", true), streamPositionCallback, context);
    }

    @Override
    public void onPause() {
        if (playbackLocation == PlaybackLocation.LOCAL) {
            if (playbackState == PlaybackState.PLAYING) {
                onPlayerStopped();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (currentCastSession != null && currentCastSession.isConnected()) {
            updatePlaybackLocation(PlaybackLocation.REMOTE);
            deviceType = "chromecast";
        } else {
            updatePlaybackLocation(PlaybackLocation.LOCAL);
            deviceType = "";
        }
        super.onResume();
        if (playbackLocation == PlaybackLocation.LOCAL) {
            player.seekTo(lastKnownRemoteStreamPosition);
            lastKnownRemoteStreamPosition = 0;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agent.stop();
        agent.flushEventStorage();
        removeSessionsManagerListener();
        removeRemoteMediaClientCallback(castContext.getSessionManager().getCurrentCastSession());
        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
    }

    @Override
    public void onBackPressed() {
        if (playbackState == PlaybackState.PLAYING) {
            onPlayerStopped();
        }

        removeSessionsManagerListener();
        removeRemoteMediaClientCallback(castContext.getSessionManager().getCurrentCastSession());

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    private void addSessionsManagerListener() {
        castContext.getSessionManager().addSessionManagerListener(
                sessionManagerListener, CastSession.class);
    }

    private void setupMediaClientCallback(final RemoteMediaClient remoteMediaClient) {
        mediaClientCallback = new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                if (!getTopMostActivityName().equals("com.gfk.s2s.cast.ExpandedControlsActivity")) {
                    Intent intent = new Intent(VideoPlayerActivity.this, ExpandedControlsActivity.class);
                    startActivity(intent);
                }
                if (currentCastSession.isConnected()) {
                    deviceType = "chromecast";
                } else if (currentCastSession.isDisconnected()) {
                    deviceType = "";
                }
                if (remoteMediaStatus != remoteMediaClient.getPlayerState()) {
                    if (remoteMediaClient.getPlayerState() == PLAYER_STATE_PLAYING && (remoteMediaStatus == PLAYER_STATE_PAUSED || remoteMediaStatus == PLAYER_STATE_BUFFERING)) {
                        updatePlaybackStatus(PlaybackState.PLAYING);
                        onPlayerStarted();

                    } else if (remoteMediaClient.getPlayerState() == PLAYER_STATE_PAUSED && remoteMediaStatus == PLAYER_STATE_PLAYING) {
                        onPlayerStopped();
                    }
                }
                remoteMediaStatus = remoteMediaClient.getPlayerState();
            }
        };
        remoteMediaClient.registerCallback(mediaClientCallback);
    }

    private void setupCastListener() {
        sessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {
                onApplicationDisconnecting(session);
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                currentCastSession = castSession;
                if (playbackState == PlaybackState.PLAYING) {
                    player.stop();
                    onPlayerStopped();
                    deviceType = "chromecast";
                    playVideoRemotely();
                } else {
                    deviceType = "chromecast";
                    updatePlaybackStatus(PlaybackState.IDLE);
                    updatePlaybackLocation(PlaybackLocation.REMOTE);
                }
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                if (playbackState == PlaybackState.PLAYING) {
                    onPlayerStopped();
                }
                deviceType = "";
                updatePlaybackStatus(PlaybackState.IDLE);
            }

            private void onApplicationDisconnecting(CastSession session) {
                RemoteMediaClient remoteMediaClient = removeRemoteMediaClientCallback(session);
                if (remoteMediaClient != null) {
                    lastKnownRemoteStreamPosition = remoteMediaClient.getApproximateStreamPosition();
                }
            }
        };
    }

    private RemoteMediaClient removeRemoteMediaClientCallback(CastSession session) {
        if (session == null) {
            return null;
        }
        final RemoteMediaClient remoteMediaClient = session.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return null;
        }
        remoteMediaClient.unregisterCallback(mediaClientCallback);

        return remoteMediaClient;
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        playbackLocation = location;
    }

    private void updatePlaybackStatus(PlaybackState playbackState) {
        this.playbackState = playbackState;
    }

    private void playVideoRemotely() {
        if (currentCastSession == null) {
            return;
        }
        final RemoteMediaClient remoteMediaClient = currentCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        setupMediaClientCallback(remoteMediaClient);
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(buildMediaInfo())
                .setAutoplay(false)
                .setCurrentTime(player.getCurrentPosition()).build());
        updatePlaybackLocation(PlaybackLocation.REMOTE);
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, content.title);

        return new MediaInfo.Builder(content.url)
                .setStreamType(content.contentType == ContentAdapter.ContentType.MovieVOD ? MediaInfo.STREAM_TYPE_BUFFERED : MediaInfo.STREAM_TYPE_LIVE)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .build();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        super.onPlayerStateChanged(playWhenReady, playbackState);

        switch (playbackState) {
            case ExoPlayer.STATE_ENDED:
                onPlayerStopped();
                break;
            case ExoPlayer.STATE_READY:
                if (!hasVideoLoaded) {
                    hasVideoLoaded = true;
                    mediaController.show();
                    findViewById(R.id.loading).setVisibility(View.GONE);
                }
                if (playWhenReady) {
                    if (currentCastSession != null && currentCastSession.isConnected()) {
                        playVideoRemotely();
                    } else {
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                        onPlayerStarted();
                        updatePlaybackStatus(PlaybackState.PLAYING);
                    }
                } else {
                    if (currentCastSession == null || !currentCastSession.isConnected()) {
                        updatePlaybackLocation(PlaybackLocation.LOCAL);
                    }
                    if (this.playbackState == PlaybackState.PLAYING) {
                        onPlayerStopped();
                    }
                }
                break;
        }
    }

    private void onPlayerStarted() {
        HashMap<String, Object> customParams = (HashMap<String, Object>) getIntent().getSerializableExtra(CUSTOM_PARAMS);
        final ContentAdapter contentAdapter = new ContentAdapter(this);
        ContentAdapter.Content content = contentAdapter.getItem(currentVideoPos);
        HashMap<String, String> options = new HashMap<>();
        options.put("screen", "fullscreen");
        options.put("volume", String.valueOf(getVolume()));
        if (!deviceType.isEmpty()) {
            options.put("deviceType", deviceType);
        }
        Log.d("castingDemoApp", "---------------------------------SEND PLAY, DEVICE_TYPE: "+ deviceType + "----------------------------------------");
        switch (content.contentType) {
            case MovieLive:
                agent.playStreamLive(contentId, "", getOffset(), content.url, options, customParams);
                break;

            case MovieVOD:
                agent.playStreamOnDemand(contentId, content.url, options, customParams);
                break;

        }
        showSkipButton();
    }

    private void onPlayerStopped() {
        Log.d("castingDemoApp", "---------------------------------SEND STOP----------------------------------------");
        updatePlaybackStatus(PlaybackState.PAUSED);
        agent.stop();
    }

    private void removeSessionsManagerListener() {
        castContext.getSessionManager().removeSessionManagerListener(
                sessionManagerListener, CastSession.class);
    }

    @Override
    public void onPlayerSeeked(int position) {
        super.onPlayerSeeked(position);
        onPlayerStopped();
    }

    private String getTopMostActivityName() {
        try {
            ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (activityManager != null) {
                    return activityManager.getAppTasks().get(0).getTaskInfo().topActivity.getClassName();
                }
            } else {
                if (activityManager != null) {
                    return activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                }
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
