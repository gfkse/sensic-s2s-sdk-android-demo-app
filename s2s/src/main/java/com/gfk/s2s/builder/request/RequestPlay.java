package com.gfk.s2s.builder.request;

public class RequestPlay extends RequestBase {

    private static final String PL = "PL";

    public String getRequestType() {
        return PL;
    }

    public void setContentId(String value) {
        fields.put("c", value);
    }

    public void setScreen(String value) {
        fields.put("sc", value);
    }

    public void setVolume(String value) {
        fields.put("vo", value);
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
