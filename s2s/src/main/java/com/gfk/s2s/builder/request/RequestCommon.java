package com.gfk.s2s.builder.request;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class RequestCommon implements IRequest {
    protected HashMap<String, String> fields = new HashMap();

    RequestCommon() {
        fields.put("ty", getRequestType());
    }

    public abstract String getRequestType();

    public void setProjectId(String value) {
        fields.put("p", value);
    }

    public void setMediaId(String value) {
        fields.put("m", value);
    }

    public void setTechnology(String value) {
        fields.put("t", value);
    }

    public void setVersion(String value) {
        fields.put("v", value);
    }

    public void setOperatingSystem(String value) {
        fields.put("os", value);
    }

    public void setAppType(String value) {
        fields.put("at", value);
    }

    public void setDeviceType(String value) {
        fields.put("dt", value);
    }

    public void setCustomParameter(HashMap customObject) {

        if (customObject == null) {
            return;
        }

        //const userParams: { [index: string]: string; } = customObject["userParams"];
        HashMap<String, String> userParams = (HashMap) customObject.get("userParams");

        if (userParams == null) {
            return;
        }

        //const allowedParams: string[] = customObject["allowedParams"];
        ArrayList<String> allowedParams = (ArrayList) customObject.get("allowedParams");

        String prefix = "cp_";
        for (String key : userParams.keySet()) {
            if (allowedParams.contains(key)) {
                fields.put(prefix + key, userParams.get(key));
            }
        }
    }

    public String getAsUrlString() {
        StringBuilder urlFields = new StringBuilder();
        for (String key : fields.keySet()) {
            String field = fields.get(key);
            String value = Uri.encode(field, "UTF-8");
            urlFields.append(key).append("=").append(value).append("&");
        }

        return urlFields.substring(0, urlFields.length() - 1);
    }
}
