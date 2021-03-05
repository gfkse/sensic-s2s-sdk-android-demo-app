package com.gfk.s2s.utils.android.factory;

import android.content.Intent;
import android.net.Uri;

public class IntentFactory {

    public static Intent build(String action, Uri uri) {
        return new Intent(action, uri);
    }

    public static Intent build(String action) {
        return new Intent(action);
    }
}
