package com.gfk.s2s.utils.http;

import androidx.annotation.Nullable;

import com.gfk.s2s.utils.HTTPTask;
import com.gfk.s2s.utils.IHttpClientFullCallback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class GetTask extends HTTPTask {
    private final IHttpClientFullCallback<String> caller;
    @Nullable private String sui;

    public GetTask(String url, @Nullable final String encodedSui, IHttpClientFullCallback<String> callback) {
        super(url);
        this.caller = callback;
        this.sui = encodedSui;
    }

    @Override
    protected void setupConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setRequestMethod("GET");
        urlConnection.setUseCaches(false);
        urlConnection.setDefaultUseCaches(false);
        if (this.sui != null) {
            urlConnection.setRequestProperty("Cookie", "sui=" + this.sui);
        }
    }

    @Override
    protected void readResponse(int responseCode, Map<String, List<String>> headers) throws IOException {
        if (responseCode == HttpsURLConnection.HTTP_OK)
            caller.onCompletion(getResponseAsString());
        else
            onError(new RuntimeException("Http error: " + responseCode));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.caller.onFinished();
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onError(Exception e) {
        caller.onCompletion(null);
    }
}
