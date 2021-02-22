package com.gfk.s2s.ui.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.IntentHelper;
import com.gfk.s2s.demo.s2s.R;
import com.gfk.s2s.s2sagent.S2SAgent;
import com.gfk.s2s.ui.videolist.ContentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContentActivity extends AppCompatActivity {

    public static final String PARAM_VIDEO = "content";
    private static final String CUSTOM_PARAMS = "pt";
    private static final String CURRENT_VIDEO = "pos";
    public static final String AGENT = "agent";
    private WebView webView;
    private S2SAgent agent;

    public static Intent newIntent(Context context, ArrayList<ContentAdapter.Content> contents, int currentVideo, HashMap<String, String> videoParams) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putParcelableArrayListExtra(PARAM_VIDEO, contents);
        intent.putExtra(CURRENT_VIDEO, currentVideo);
        intent.putExtra(CUSTOM_PARAMS, videoParams);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ArrayList<ContentAdapter.Content> contents = getIntent().getParcelableArrayListExtra(PARAM_VIDEO);
        int currentVideoPos = getIntent().getIntExtra(CURRENT_VIDEO, 0);
        ContentAdapter.Content content = contents.get(currentVideoPos);

        super.onCreate(savedInstanceState);

        agent = (S2SAgent) IntentHelper.getObjectForKey(AGENT);

        setContentView(R.layout.activity_content);
        webView = findViewById(R.id.webView);
        webView.setWebViewClient( new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(content.url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState ) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Map<String, Object> customParams = (Map<String, Object>) getIntent().getSerializableExtra(CUSTOM_PARAMS);
            agent.impression(url, customParams);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }
}
