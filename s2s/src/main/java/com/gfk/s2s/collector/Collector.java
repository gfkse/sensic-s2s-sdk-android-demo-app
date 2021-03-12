package com.gfk.s2s.collector;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.gfk.s2s.BuildConfig;
import com.gfk.s2s.collector.utils.DeviceInfo;
import com.gfk.s2s.utils.GlobalConst;
import com.gfk.s2s.utils.HTTPClient;
import com.gfk.s2s.utils.IHttpClientFullCallback;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Collector implements ICollector {

    public static final String SHARED_PREFS_FILE = "Settings";
    public static final String SUI_ID = "suiId";
    private static final String DEVICE_TYPE_UNKNOWN = "UNKNOWN";
    public static final String OPERATING_SYSTEM = "Android";
    private static final String TRK_REQUEST_URL = "https://<suiid>.trk.sensic.net/tp.gif";

    private final Context context;
    private Config config;
    private String sui = "";
    private boolean isConfigLoaded = false;
    private String advertisingId;
    private long timeOffset = 0;
    private String deviceType = DEVICE_TYPE_UNKNOWN;
    private ICollectorSuiAvailableCallback suiCallback;
    private boolean optin = true;

    public void setCollectorSuiCallback(ICollectorSuiAvailableCallback suiCallback) {
        this.suiCallback = suiCallback;
    }

    private boolean isOptin() {
        return optin;
    }

    public void setOptin(boolean optin) {
        this.optin = optin;
    }

    @VisibleForTesting
    public void setSUI(String sui) {
        this.sui = sui;
    }

    public String getSui() {
        return sui;
    }

    private void setSui(String sui) {
        this.sui = sui;
    }

    public Collector(Context context) {
        this.context = context;
        setConfig(null);

        new GetGAIDTask(context).execute();
    }

    public String getProjectName() {
        return config.getProjectName();
    }

    @Override
    public String getAppType() {
        return "APP";
    }

    public List<String> getStreamCustomParameter() {
        return config.getStreamCustom();
    }

    public List<String> getContentCustomParameter() {
        return config.getContentCustom();
    }

    public String getDeviceType() {
        if (context == null) return DEVICE_TYPE_UNKNOWN;
        if (!deviceType.equals(DEVICE_TYPE_UNKNOWN)){
            return deviceType;
        }
        else {
            return DeviceInfo.getDeviceType(context);
        }
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Boolean isProjectEnabled() {
        return config.isEnabled() == null ? true : config.isEnabled();
    }

    public String getTrackingUrl() {
        return config.getTrackingUrl();
    }

    public String getSuiUrl() {
        return config.getSuiGeneratorUrl();
    }

    public String getLanguage() {
        return Locale.getDefault().toString();
    }

    public String getUserAgent() {
        String version = Build.VERSION.RELEASE;
        return OPERATING_SYSTEM + " " + version + "/" + getLanguage();
    }

    public String getTechnology() {
        return config.getTech();
    }

    public String getVersion() {
        return config.getProjectVersion() + "/" + BuildConfig.VERSION_APP_NAME + "/" + config.getConfigVersion();
    }

    public String getOrigin() {
        CharSequence name = context.getApplicationInfo().name;
        return name == null ? "" : name.toString();
    }

    public String getAdvertisingId() {
        return advertisingId;
    }

    public void loadConfig(String configUrl, final ICollectorConfigCallback callback) {
        HTTPClient.get(configUrl, new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(final String data) {
                if (isConfigLoaded) return;

                final Config newConfig;
                if (data == null) {
                    newConfig = new Config();
                    newConfig.setEnabled(false);
                } else {
                    newConfig = Config.createFromJson(data);
                }

                loadServerTime(newConfig.getTsUrl(), () -> {
                    Collector.this.config = newConfig;
                    isConfigLoaded = true;
                    callback.onCompletion(newConfig, data != null);
                    callback.onFinished();
                });
            }

            @Override
            public void onFinished() {
                //onFinished not needed in this case
            }
        });
    }

    private void loadServerTime(String url, final ICollectorTimestampCallback callback) {
        HTTPClient.head(url, new IHttpClientFullCallback<Calendar>() {
            @Override
            public void onCompletion(Calendar serverTime) {
                if (serverTime != null) {
                    timeOffset = serverTime.getTimeInMillis() - getUTCTimestamp();
                }
            }

            @Override
            public void onFinished() {
                callback.onFinished();
            }
        });
    }

    public void loadSui() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String cachedSui = sharedPreferences.getString(SUI_ID, null);
        HTTPClient.get(getSuiUrlWithParameters(), cachedSui, new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                if (data == null) return;
                if (!data.isEmpty()) {
                    try {
                        JSONObject responseJSON = new JSONObject(data);
                        String suiId = responseJSON.getString("id");
                        int lifetime = responseJSON.getInt("lt");
                        manageSuiStorage(lifetime, data);
                        if (!suiId.isEmpty()) {
                            setSui(data);
                            if (suiCallback != null) {
                                suiCallback.onSuiAvailable();
                                suiCallback = null;
                            }
                            fireTpRequest(suiId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinished() {
                //onFinished not needed in this case
            }
        });
    }

    private void manageSuiStorage(int lifetime, String sui) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (lifetime > 0 && !sui.equals(prefs.getString(SUI_ID, null))) {
            editor.putString(SUI_ID, sui);
        }
        if (lifetime <= 0) {
            editor.remove(SUI_ID);
        }

        editor.apply();
    }

    private void fireTpRequest(String id) {
        String url = TRK_REQUEST_URL.replace("<suiid>", id);
        url = addParameters(url);
        HTTPClient.get(url, new IHttpClientFullCallback<String>() {
            @Override
            public void onCompletion(String data) {
                //onCompletion not needed in this case
            }

            @Override
            public void onFinished() {
                //onFinished not needed in this case
            }
        });
    }

    @VisibleForTesting
    String addParameters(String url) {
        url = url + "?r=" + Uri.encode(getPackageName(), "UTF-8");
        url = url + "&p=" + Uri.encode(getProjectConfig(), "UTF-8");

        return url;
    }

    private String getProjectConfig() {
        String suiGeneratorUrl = config.getSuiGeneratorUrl();
        if (suiGeneratorUrl.isEmpty()) return "";

        String host;
        try {
            host = new URL(suiGeneratorUrl).getHost();
        } catch (MalformedURLException e) {
            Log.e(GlobalConst.LOG_TAG, e.getMessage());
            return "";
        }
        if (host.isEmpty()) return "";

        return host.replace(".sensic.net", "");
    }

    private String getPackageName() {
        return context.getPackageName();
    }

    public void setConfig(Config config) {
        if (config == null) {
            this.config = new Config();
        } else {
            this.config = config;
        }
    }

    private String getSuiUrlWithParameters() {
        return getSuiUrl() + "?dt="
                + Uri.encode( getDeviceType())
                + "&o=" + Uri.encode(OPERATING_SYSTEM)
                + "&t=" + Uri.encode(getTechnology())
                + "&ai=" + Uri.encode(getAdvertisingId())
                + "&optin=" + isOptin()
                + "&f=json";
    }

    public SegmentConfig getSegmentConfig() {
        return this.config.getSegmentConfig();
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public long getTimeWithOffset() {
        return getUTCTimestamp() + timeOffset;
    }

    public long getUTCTimestamp() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSSZ");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        final String utcTime = dateFormatGmt.format(new Date());

        //Local time zone
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss:SSSZ");

        try {
            return dateFormatLocal.parse(utcTime).getTime();
        } catch (ParseException e) {
            return new Date().getTime();
        }
    }

    private class GetGAIDTask extends AsyncTask<String, Integer, String> {

        private static final String LIMIT_AD_TRACKING = "limit_ad_tracking";
        private static final String ADVERTISING_ID = "advertising_id";
        private Context context;

        GetGAIDTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            if (DeviceInfo.isFireTvDevice(context)) {
                try {
                    ContentResolver cr = context.getContentResolver();
                    // check if user has opted out of tracking
                    if (Settings.Secure.getInt(cr, LIMIT_AD_TRACKING) != 0) return "";

                    return Settings.Secure.getString(cr, ADVERTISING_ID);
                } catch (Settings.SettingNotFoundException ex) {
                    return "";
                }
            } else {
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

        @Override
        protected void onPostExecute(String s) {
            advertisingId = s;
        }
    }

}
