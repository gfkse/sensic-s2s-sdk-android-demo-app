package com.gfk.s2s.utils.http;

import com.gfk.s2s.utils.HTTPTask;
import com.gfk.s2s.utils.IHttpClientCallback;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class PostTask extends HTTPTask {
    private final IHttpClientCallback caller;
    private final String postData;

    public PostTask(String url, String postData, IHttpClientCallback callback) {
        super(url);
        this.caller = callback;
        this.postData = postData;
    }

    @Override
    protected void setupConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setDoOutput(true);
    }

    @Override
    protected void writeRequest(HttpURLConnection urlConnection) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
        outputStreamWriter.write(postData);
        outputStreamWriter.close();
    }

    @Override
    protected void readResponse(int responseCode, Map<String, List<String>> headers) {
        caller.onCompletion(responseCode == HttpsURLConnection.HTTP_NO_CONTENT);
    }

    @Override
    protected void onError(Exception e) {
        caller.onCompletion(false);
    }
}
