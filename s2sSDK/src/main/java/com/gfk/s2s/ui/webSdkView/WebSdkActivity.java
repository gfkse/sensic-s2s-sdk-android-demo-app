package com.gfk.s2s.ui.webSdkView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.demo.s2s.R;

public class WebSdkActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, WebSdkActivity.class);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_sdk);

        WebView view = findViewById(R.id.webSdkView_webView);
        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("https://demo-config-preproduction.sensic.net")) {
                    view.loadUrl(url);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.loadUrl("https://demo-config-preproduction.sensic.net/s2s/index.html");
    }
}