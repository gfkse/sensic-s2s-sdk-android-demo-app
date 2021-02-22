package com.gfk.s2s.registration;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gfk.s2s.utils.android.factory.IntentFactory;

import static com.gfk.s2s.registration.RegistrationUtils.runJavascript;
import static com.gfk.s2s.registration.RegistrationUtils.setAppIconInsideWebView;


public class RegistrationWebViewClient extends WebViewClient {

    private Context context;
    private ViewGroup rootView;

    public RegistrationWebViewClient(Context context, ViewGroup rootView) {
        this.context = context;
        this.rootView = rootView;
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        setAppIconInsideWebView(webView, context.getApplicationContext());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        Intent intent = IntentFactory.build(Intent.ACTION_VIEW, Uri.parse(url));
        if (url.contains("target=exit")) {
            rootView.removeView(webView);
            webView.getContext().startActivity(intent);
            return true;
        } else if ((url.startsWith("https") || url.startsWith("http"))) {
            webView.loadUrl(url);
        } else {
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                runJavascript("window.cancelProcess();", webView);
                rootView.removeView(webView);
                context.startActivity(intent);
            }
        }

        return true;
    }
}
