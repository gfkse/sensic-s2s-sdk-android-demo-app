/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gfk.s2s.ui.details;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.SettingsContentObserver;
import com.gfk.s2s.demo.s2s.R;
import com.gfk.s2s.s2sagent.S2SAgent;
import com.gfk.s2s.s2sagent.StreamPositionCallback;
import com.gfk.s2s.ui.videolist.ContentAdapter;
import com.gfk.s2s.ui.videolist.offset.OffsetDialog;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.util.PlayerControl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An activity that plays media using {@link ExoPlayer}. Based on
 * https://github.com/google/ExoPlayer/blob/master/demo/src/main/java/com/google/android/exoplayer/demo/PlayerActivity.java
 */
abstract class BasePlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        ExoPlayer.Listener, MediaCodecVideoTrackRenderer.EventListener {

    public static final String PARAM_VIDEO = "content";
    protected static final String CUSTOM_PARAMS = "pt";
    protected static final String CURRENT_VIDEO = "pos";
    public static final int RENDERER_COUNT = 2;
    private static final String TAG = "PlayerActivity";

    private Handler mainHandler;
    private View shutterView;
    private VideoSurfaceView surfaceView;
    protected MediaController mediaController;
    protected ExoPlayer player;
    private MediaCodecVideoTrackRenderer videoRenderer;
    private boolean autoPlay = false;
    private long playerPosition;
    protected Uri contentUri;
    protected String contentId;
    private Button skipButton;
    protected ArrayList<ContentAdapter.Content> contents;
    protected int currentVideoPos;
    protected ContentAdapter.Content content;
    protected boolean hasVideoLoaded;
    protected S2SAgent agent;
    protected StreamPositionCallback streamPositionCallback;
    protected SettingsContentObserver settingsContentObserver;

    public static Intent newIntent(Context context, ArrayList<ContentAdapter.Content> contents, int currentVideo, HashMap<String, String> videoParams, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.putParcelableArrayListExtra(PARAM_VIDEO, contents);
        intent.putExtra(CURRENT_VIDEO, currentVideo);
        intent.putExtra(CUSTOM_PARAMS, videoParams);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contents = getIntent().getParcelableArrayListExtra(PARAM_VIDEO);
        currentVideoPos = getIntent().getIntExtra(CURRENT_VIDEO, 0);
        content = contents.get(currentVideoPos);
        hasVideoLoaded = false;
        resetOffset();
        contentUri = getContentURI();
        contentId = "Entertainment";

        mainHandler = new Handler(getMainLooper());

        setContentView(R.layout.activity_demo_player);
        View root = findViewById(R.id.root);
        root.setOnTouchListener((arg0, arg1) -> {
            if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                if (mediaController.isShowing())
                    mediaController.hide();
                else
                    mediaController.show(0);
            }
            return true;
        });

        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);
        shutterView = findViewById(R.id.shutter);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        skipButton = root.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(view -> onSkipButtonClick());

        Button screenButton = root.findViewById(R.id.screenButton);
        screenButton.setOnClickListener(view -> onScreenButtonClick());

        Button offsetButton = root.findViewById(R.id.offsetButton);
        offsetButton.setOnClickListener(view -> onOffsetButtonClick());

        streamPositionCallback = () -> {
            if (player == null) {
                return 0;
            }

            return (int)player.getCurrentPosition();
        };

        settingsContentObserver = new SettingsContentObserver(this,new Handler()) {
            @Override
            public void louder(int currentVolume) {
                super.louder(currentVolume);
                agent.volume(""+currentVolume);
            }

            @Override
            public void quieter(int currentVolume) {
                super.quieter(currentVolume);
                agent.volume(""+currentVolume);
            }
        };
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup the player
        player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        player.addListener(this);
        player.seekTo(playerPosition);
        // Build the player controls
        PlayerControl ctrl = new PlayerControl(this.player) {
            @Override
            public void seekTo(int timeMillis) {
                super.seekTo(timeMillis);
                onPlayerSeeked(timeMillis);
            }
        };
        mediaController.setMediaPlayer(ctrl);
        mediaController.setEnabled(true);
        buildRenderers(contentUri);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Release the player
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
        videoRenderer = null;
        shutterView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        agent.stop();
        agent.flushEventStorage();
        super.onStop();
    }

    protected void onSkipButtonClick() {
        nextVideo();
        resetPlayerForSkip();
        agent.skip();
    }

    private void nextVideo() {
        currentVideoPos++;
        if(currentVideoPos == contents.size()){
            currentVideoPos = 0;
        }
        content = contents.get(currentVideoPos);
    }

    private void resetPlayerForSkip() {
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        hasVideoLoaded = false;
        player.seekTo(0);
        buildRenderers(contentUri);
    }

    protected void onScreenButtonClick() {
        agent.screen("ScreenButton");
    }

    protected void onOffsetButtonClick() {
        OffsetDialog offsetDialog = new OffsetDialog();
        Bundle args = new Bundle();
        offsetDialog.setArguments(args);
        offsetDialog.show(getSupportFragmentManager(), "offsetDialogTag");
    }

    protected void showSkipButton() {
        skipButton.setVisibility(View.VISIBLE);
    }

    public void buildRenderers(Uri uri) {
        FrameworkSampleSource sampleSource = new FrameworkSampleSource(this, uri, null, 2);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, this.getMainHandler(), this, 50);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        onRenderers(videoRenderer, audioRenderer);
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    private Uri getContentURI() {
        return Uri.parse(content.url);
    }

    private void onRenderers(MediaCodecVideoTrackRenderer videoRenderer, MediaCodecAudioTrackRenderer audioRenderer) {
        this.videoRenderer = videoRenderer;
        player.prepare(videoRenderer, audioRenderer);
        maybeStartPlayback();
    }

    private void maybeStartPlayback() {
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            player.setPlayWhenReady(true);
            autoPlay = false;
        }
    }

    protected int getVolume() {
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        return 0;
    }

    protected int getOffset() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        return preferences.getInt("offset", 0);
    }

    private void onError(Exception e) {
        Log.e(TAG, "Playback failed", e);
        Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void resetOffset() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("offset", 0);
        editor.apply();
    }

    // ExoPlayer.Listener implementation
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // Do nothing.
    }

    public void onPlayerSeeked(int position) {
        // Do nothing.
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        onError(e);
    }

    // MediaCodecVideoTrackRenderer.Listener
    @Override
    public void onDrawnToSurface(Surface surface) {
        shutterView.setVisibility(View.GONE);
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "Dropped frames: " + count);
    }

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {
        // This is for informational purposes only. Do nothing.
    }

    @Override
    public void onCryptoError(CryptoException e) {
        // This is for informational purposes only. Do nothing.
    }

    // SurfaceHolder.Callback implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        maybeStartPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        surfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    @Override
    public void onDecoderInitialized(String s, long l, long l1) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoRenderer != null) {
            player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }
}
