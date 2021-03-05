package com.gfk.s2s.utils.factory;

import android.net.Uri;

import com.gfk.s2s.registration.RegistrationQueryParams;

public class RegistrationQueryParamsFactory {

    public static RegistrationQueryParams build(Uri uri) {
        return new RegistrationQueryParams(uri);
    }
}
