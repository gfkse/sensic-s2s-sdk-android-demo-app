package com.gfk.s2s.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.gfk.s2s.Endpoint;

public class EndpointHelper {

    private static final String DEMO_ENDPOINT = "https://demo-config.sensic.net/s2s-android.json";
    private static final String DEMO_PRE_PROD_ENDPOINT = "https://demo-config-preproduction.sensic.net/s2s-android.json";

    public static String getEndpointUrl(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Endpoint endpoint = Endpoint.fromId(preferences.getInt("endpoint", 1));
        if (endpoint != null) {
            switch (endpoint) {
                case DEMO:
                    return DEMO_ENDPOINT;
                case PREPROD:
                    return DEMO_PRE_PROD_ENDPOINT;
            }
        }

        return DEMO_PRE_PROD_ENDPOINT;
    }
}
