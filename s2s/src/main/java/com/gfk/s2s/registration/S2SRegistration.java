package com.gfk.s2s.registration;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.gfk.s2s.utils.GlobalConst;
import com.gfk.s2s.utils.StateSchemeHelper;
import com.gfk.s2s.utils.factory.RegistrationQueryParamsFactory;
import com.gfk.s2s.utils.factory.RegistrationWebViewClientFactory;

import org.json.JSONException;
import org.json.JSONObject;

import static com.gfk.s2s.registration.RegistrationUtils.changeJsonObject;
import static com.gfk.s2s.registration.RegistrationUtils.createWebView;
import static com.gfk.s2s.registration.RegistrationUtils.getJsonObject;
import static com.gfk.s2s.registration.RegistrationUtils.loadSui;
import static com.gfk.s2s.registration.RegistrationUtils.removeSui;

public final class S2SRegistration {

    public static void storePanelist(final ViewGroup rootView, Uri uri) {
        final Context context = rootView.getContext();
        RegistrationQueryParams queryParams;
        queryParams = RegistrationQueryParamsFactory.build(uri);
        JSONObject stateAsJsonObject = getJsonObject(queryParams.getState());
        changeJsonObject(stateAsJsonObject, queryParams.getScheme());
        queryParams.setState(stateAsJsonObject.toString());
        handleSUI(context, queryParams, stateAsJsonObject);
        WebView webview = createWebView(context);
        rootView.addView(webview);

        webview.setWebViewClient(RegistrationWebViewClientFactory.build(context, rootView));
        webview.loadUrl(queryParams.getWebViewUrl());
    }

    private static void handleSUI(Context context, RegistrationQueryParams queryParams, JSONObject stateAsJsonObject) {
        String scheme = queryParams.getScheme();
        if (scheme == null) {
            Log.e(GlobalConst.LOG_TAG, "Scheme of URI is null.");
            return;
        }
        String stateObjectKey = StateSchemeHelper.findStateKeyForScheme(stateAsJsonObject, scheme);
        if (stateObjectKey == null) {
            Log.e(GlobalConst.LOG_TAG, "Scheme " + scheme + " not found as key in apps state object.");
            return;
        }

        try {
            if (stateAsJsonObject.get(stateObjectKey).equals(AppState.CONNECTED.toString())) {
                loadSui(queryParams, context);
            } else if (stateAsJsonObject.get(stateObjectKey).equals(AppState.DISCONNECTED.toString())) {
                removeSui(context);
            }
        } catch (JSONException e) {
            Log.e(GlobalConst.LOG_TAG, "JSONException could not find " + stateObjectKey + " in state object. " + e);
        }
    }

    public static Boolean isSensicUrl(Uri uri) {
        if (uri == null || uri.getHost() == null) return false;

        return "sensic.net".equals(uri.getHost());
    }

}
