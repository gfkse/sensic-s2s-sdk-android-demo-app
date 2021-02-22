package com.gfk.s2s.ui.details;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.gfk.s2s.demo.s2s.R;
import com.gfk.s2s.s2sagent.S2SAgent;
import com.gfk.s2s.ui.EndpointHelper;
import com.gfk.s2s.ui.videolist.ContentAdapter;
import com.google.android.exoplayer.ExoPlayer;

import java.util.HashMap;

public class FireTvVideoPlayerActivity extends BasePlayerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String endpointUrl = EndpointHelper.getEndpointUrl(context);

        agent = new S2SAgent(endpointUrl, "s2sdemomediaid_ssa_android", preferences.getBoolean("optin", true), streamPositionCallback, context);
    }

    @Override
    public void onPause() {
        onPlayerStopped();
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        onPlayerStopped();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agent.stop();
        agent.flushEventStorage();
        getApplicationContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
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
                if (playWhenReady)
                    onPlayerStarted();
                else
                    onPlayerStopped();
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
        agent.stop();
    }

    @Override
    public void onPlayerSeeked(int position) {
        super.onPlayerSeeked(position);
        onPlayerStopped();
    }
}
