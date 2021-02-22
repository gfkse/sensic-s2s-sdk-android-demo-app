package com.gfk.s2s.builder.request;

public class RequestImpression extends RequestCommon {

    private static final String IM = "IM";

    public String getRequestType() {
        return IM;
    }

    public void setContentId(String value) {
        fields.put("c", value);
    }

    public void setSui(String value) {
        fields.put("sui", value);
    }

    public void setOrigin(String value) {
        fields.put("r", value);
    }

    public void setUserAgent(String value) {
        fields.put("ua", value);
    }

    public void setLanguage(String value) {
        fields.put("l", value);
    }
}
