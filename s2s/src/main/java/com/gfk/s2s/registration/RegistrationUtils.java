package com.gfk.s2s.registration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.gfk.s2s.utils.GlobalConst;
import com.gfk.s2s.utils.HTTPClient;
import com.gfk.s2s.utils.IHttpClientFullCallback;
import com.gfk.s2s.utils.StateSchemeHelper;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import static android.util.Base64.encodeToString;
import static com.gfk.s2s.collector.Collector.SHARED_PREFS_FILE;
import static com.gfk.s2s.collector.Collector.SUI_ID;

class RegistrationUtils {

    static void changeJsonObject(JSONObject appStates, String scheme) {
        String appStateKey = StateSchemeHelper.findStateKeyForScheme(appStates, scheme);
        if (appStateKey == null) {
            Log.e(GlobalConst.LOG_TAG, "Scheme " + scheme + " not found as key in apps state object.");
            return;
        }
        try {
            if (appStates.get(appStateKey).equals(AppState.SHOULD_CONNECT.toString())) {
                appStates.put(appStateKey, AppState.CONNECTED.toString());
            } else if (appStates.get(appStateKey).equals(AppState.SHOULD_DISCONNECT.toString())) {
                appStates.put(appStateKey, AppState.DISCONNECTED.toString());
            }
        } catch (JSONException e){
            Log.e(GlobalConst.LOG_TAG, "JSONException could not find " + appStateKey + " in state object." + e);
        }
    }

    static JSONObject getJsonObject(String state) {
        try {
            return new JSONObject(state);
        } catch (JSONException err){
            return new JSONObject();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    static WebView createWebView(Context context) {
        WebView webview = new WebView(context);
        webview.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        return webview;
    }

    static void setAppIconInsideWebView(WebView webView, Context context) {
        final PackageManager packageManager = context.getPackageManager();
        String base64ImageString;
        try {
            final Drawable appIcon = packageManager.getApplicationIcon(context.getPackageName());
            final Bitmap bitmap = getBitmapFromDrawable(appIcon);
            base64ImageString = encodeToBase64(bitmap);
        } catch (Exception e) {
            Log.e("GfKlog", "Exception at setAppIconInsideWebView(): " + e.getMessage());
            return;
        }
        String url = "data:application/png;base64," + base64ImageString;
        String javascript = "setTimeout(() => { var imageElement = document.getElementById('publisherLogo'); imageElement.setAttribute('src', '" + url + "');}, 1000);";
        runJavascript(javascript, webView);
    }

    @NonNull
    private static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bmp;
    }

    static void runJavascript(String js, WebView webView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, null);
        } else {
            webView.loadUrl("javascript:" + js);
        }
    }

    private static String encodeToBase64(Bitmap image) {

        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);

        return encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);
    }

    static void loadSui(RegistrationQueryParams registrationQueryParams, Context context) {
        HTTPClient.get(registrationQueryParams.getEnrichedSuiGeneratorUrl(context), new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                if (data == null) return;
                if (!data.isEmpty()) {
                    try {
                        JSONObject responseJSON = new JSONObject(data);
                        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(SUI_ID, responseJSON.toString());
                        editor.apply();
                        Log.d(GlobalConst.LOG_TAG, "Successfully set SUI object in App Cache: " + responseJSON.toString());
                    } catch (JSONException e) {
                        Log.e(GlobalConst.LOG_TAG, "Exception while parsing data to JSON in loadSui()");
                    }
                }
            }

            @Override
            public void onFinished() {
                //onFinished not needed in this case
            }
        });
    }

    static void removeSui(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SUI_ID);
        editor.apply();
        Log.d(GlobalConst.LOG_TAG, "Successfully removed SUI object from App Cache");
    }

    public static String getAdvertisingId(Context context) {
        AdvertisingIdClient.Info adInfo;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            if (adInfo == null) return "";
            // check if user has opted out of tracking
            if (adInfo.isLimitAdTrackingEnabled()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        return adInfo.getId();
    }
}
