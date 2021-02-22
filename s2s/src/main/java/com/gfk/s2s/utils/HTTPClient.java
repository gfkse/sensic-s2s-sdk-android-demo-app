package com.gfk.s2s.utils;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.gfk.s2s.utils.http.GetTask;
import com.gfk.s2s.utils.http.HeadTask;
import com.gfk.s2s.utils.http.PostTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public final class HTTPClient {

    private HTTPClient() {
        throw new IllegalStateException("Utility class");
    }

    public static class Instantiable {
        public void post(String urlString, String postData, IHttpClientCallback callback) {
            HTTPClient.post(urlString, postData, callback);
        }
    }

    public static void get(String urlString, IHttpClientFullCallback<String> callback) {
        get(urlString, 3, null, callback);
    }

    public static void get(String urlString, @Nullable final String sui, IHttpClientFullCallback<String> callback) {
        get(urlString, 3, sui, callback);
    }

    public static void get(String urlString, final Integer retry, IHttpClientFullCallback<String> callback) {
        get(urlString, retry, null, callback);
    }

    public static void get(final String urlString, final Integer retry, @Nullable final String sui, final IHttpClientFullCallback<String> callback) {
        GetTask get;
        final long timeoutInterval = 20;
        String encodedSui = null;
        if (sui != null && !sui.isEmpty()) {
            encodedSui = Uri.encode(sui, "UTF-8");
        }

        try {
            if (retry > 1) {
                get = new GetTask(urlString, encodedSui, new IHttpClientFullCallback<String>() {
                    @Override
                    public void onCompletion(String data) {
                        if (data == null) {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    get(urlString, retry - 1, sui,  callback);
                                }
                            }, timeoutInterval * 1000);
                        } else {
                            callback.onCompletion(data);
                        }
                    }

                    @Override
                    public void onFinished() {
                        callback.onFinished();
                    }
                });
            } else {
                get = new GetTask(urlString, encodedSui, callback);
            }

            get.execute();

        } catch (Exception e) {
            callback.onCompletion(null);
        }
    }

    public static void post(String urlString, String postData, IHttpClientCallback callback) {
        final PostTask post = new PostTask(urlString, postData, callback);
        post.execute();
    }

    public static void head(String urlString, IHttpClientFullCallback<Calendar> callback) {
        HeadTask head = new HeadTask(urlString, callback);
        head.execute();
    }
}
