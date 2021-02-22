package com.gfk.s2s.ui.videolist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.IntentHelper;
import com.gfk.s2s.collector.utils.DeviceInfo;
import com.gfk.s2s.demo.s2s.R;
import com.gfk.s2s.s2sagent.S2SAgent;
import com.gfk.s2s.ui.EndpointHelper;
import com.gfk.s2s.ui.details.ContentActivity;
import com.gfk.s2s.ui.details.FireTvVideoPlayerActivity;
import com.gfk.s2s.ui.details.SettingsActivity;
import com.gfk.s2s.ui.details.VideoPlayerActivity;
import com.gfk.s2s.ui.pixel.PixelRequestActivity;
import com.gfk.s2s.ui.videolist.videoparams.VideoParamsDialog;
import com.gfk.s2s.ui.videolist.videoparams.VideoParamsWrapper;
import com.gfk.s2s.ui.webSdkView.WebSdkActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class ContentSelectionActivity extends AppCompatActivity implements VideoParamsDialog.VideoParamManager {

    private S2SAgent customAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        loadVideoParamsFromPreferences();
        getSupportActionBar().setTitle("Demo Content Player Selection");
        final Context context = getApplicationContext();
        String endpointUrl = EndpointHelper.getEndpointUrl(context);

        customAgent = new S2SAgent(endpointUrl, "s2sdemomediaid_sst_android", context);

        final ListView list = findViewById(R.id.list);
        final ContentAdapter contentAdapter = new ContentAdapter(this);
        list.setAdapter(contentAdapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            ContentAdapter.Content content = contentAdapter.getItem(position);

            switch (content.contentType) {
                case MovieLive:
                case MovieVOD: {
                    Intent intent;
                    if (DeviceInfo.isFireTvDevice(context) || !checkPlayServices()) {
                        intent = FireTvVideoPlayerActivity.newIntent(view.getContext(), contentAdapter.getContents(), position, VideoParamsWrapper.getInstance().getVideoParams(), FireTvVideoPlayerActivity.class);
                    } else {
                        intent = VideoPlayerActivity.newIntent(view.getContext(), contentAdapter.getContents(), position, VideoParamsWrapper.getInstance().getVideoParams(), VideoPlayerActivity.class);
                    }
                    startActivity(intent);
                    break;
                }

                case Content: {
                    IntentHelper.addObjectForKey(customAgent, ContentActivity.AGENT);
                    Intent intent = ContentActivity.newIntent(view.getContext(), contentAdapter.getContents(), position, VideoParamsWrapper.getInstance().getVideoParams());
                    startActivity(intent);
                    break;
                }
                case Settings: {
                    Intent intent = SettingsActivity.newIntent(view.getContext());
                    startActivity(intent);
                    break;
                }
                case WebSdkView: {
                    Intent intent = WebSdkActivity.newIntent(view.getContext());
                    startActivity(intent);
                    break;
                }
                case PixelRequest: {
                    Intent intent = PixelRequestActivity.Companion.newIntent(view.getContext());
                    startActivity(intent);
                    break;
                }
            }
        });
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_selection_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_params) {
            VideoParamsDialog paramDialog = new VideoParamsDialog();
            Bundle args = new Bundle();
            args.putStringArrayList(VideoParamsDialog.PARAMS_EXTRA, new ArrayList<String>());
            paramDialog.setArguments(args);
            paramDialog.show(getSupportFragmentManager(), "videoDialogTag");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public HashMap<String, String> getVideoParams() {
        return VideoParamsWrapper.getInstance().getVideoParams();
    }

    @Override
    public void writeVideoParamsToPreferences() {
        Gson gson = new Gson();
        VideoParamsWrapper wrapper = VideoParamsWrapper.getInstance();
        String serializedMap = gson.toJson(wrapper);
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        prefs.edit().putString(VideoParamsWrapper.VIDEO_PARAMS, serializedMap).apply();
    }

    public void loadVideoParamsFromPreferences() {
        String wrapperStr = this.getPreferences(MODE_PRIVATE).getString(VideoParamsWrapper.VIDEO_PARAMS, null);
        if (wrapperStr != null) {
            VideoParamsWrapper wrapper = new Gson().fromJson(wrapperStr, VideoParamsWrapper.class);
            VideoParamsWrapper.getInstance().setVideoParams(wrapper.getVideoParams());
        }
    }
}
