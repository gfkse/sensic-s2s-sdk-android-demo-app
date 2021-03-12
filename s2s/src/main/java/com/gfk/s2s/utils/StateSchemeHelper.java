package com.gfk.s2s.utils;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Iterator;

public final class StateSchemeHelper {

    private StateSchemeHelper() {
        //Do nothing
    }

    /**
     * Finds a matching key in the apps state object for the current scheme, ignoring case differences.
     * So a key in state object like 'ORFSport' will match for the scheme in 'orfsport://sensic.net'.
     * @param state is a key value pair. Key is the name of the app url. Value is the current state of connection.
     * @param scheme is the first part of an domain.
     * @return The key to index the apps state object or null if nothing matched.
     */
    public static @Nullable String findStateKeyForScheme(JSONObject state, String scheme) {
        Iterator<String> keys = state.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.toLowerCase().equals(scheme.toLowerCase())) {
                return key;
            }
        }
        return null;
    }
}
