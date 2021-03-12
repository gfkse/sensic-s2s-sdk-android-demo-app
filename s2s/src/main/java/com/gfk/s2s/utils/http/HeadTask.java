package com.gfk.s2s.utils.http;

import com.gfk.s2s.utils.HTTPTask;
import com.gfk.s2s.utils.IHttpClientFullCallback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HeadTask extends HTTPTask {

    private final IHttpClientFullCallback<Calendar> caller;

    public HeadTask(String url, IHttpClientFullCallback<Calendar> callback) {
        super(url);
        this.caller = callback;
    }

    @Override
    protected void setupConnection(HttpURLConnection urlConnection) throws IOException {
        urlConnection.setRequestMethod("HEAD");
        urlConnection.setDoOutput(false);
    }

    @Override
    protected void readResponse(int responseCode, Map<String, List<String>> headers) throws IOException {
        List<String> dates = headers.get("Date");
        String date = dates != null && !dates.isEmpty() ? dates.get(0) : "";

        SimpleDateFormat formatter = new SimpleDateFormat("EEE',' dd MMM yyyy HH':'mm':'ss Z", Locale.US);


        Calendar result = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);;

        try {
            result.setTime(formatter.parse(date));
        } catch (ParseException e) {
            result.setTime(new Date());
        }

        this.caller.onCompletion(result);
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
