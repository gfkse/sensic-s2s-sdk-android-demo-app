package com.gfk.s2s.utils;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class HTTPTask extends AsyncTask<Void, Void, Void> {

    private HttpURLConnection urlConnection;
    private final String url;

    public HTTPTask(String url) {
        this.url = url;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Connection", "close");
            setupConnection(urlConnection);

            urlConnection.connect();

            writeRequest(urlConnection);
            Map<String, List<String>> headers = urlConnection.getHeaderFields();
            readResponse(urlConnection.getResponseCode(), headers);
        }
        catch (Exception e) {
            onError(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    protected abstract void setupConnection(HttpURLConnection urlConnection) throws IOException;

    protected void onError(Exception e) {}

    protected abstract void readResponse(int responseCode, Map<String, List<String>> headers) throws IOException;

    protected void writeRequest(HttpURLConnection stream) throws IOException {}

    protected String getResponseAsString() throws  IOException {
        StringBuilder response = new StringBuilder();
        String result = "";

        BufferedReader bReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while ((result = bReader.readLine()) != null) {
            response.append(result);
        }
        bReader.close();

        return  response.toString();
    }
}
