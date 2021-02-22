package com.gfk.s2s.utils.factory;

import android.content.Context;
import android.view.ViewGroup;

import com.gfk.s2s.registration.RegistrationWebViewClient;

public class RegistrationWebViewClientFactory {

    public static RegistrationWebViewClient build(Context context, ViewGroup viewGroup) {
        return new RegistrationWebViewClient(context, viewGroup);
    }
}
