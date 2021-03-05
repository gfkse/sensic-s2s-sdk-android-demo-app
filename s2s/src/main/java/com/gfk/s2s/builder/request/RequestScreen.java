package com.gfk.s2s.builder.request;

public class RequestScreen extends RequestBase {

    private static final String SC = "SC";

    public String getRequestType() {
        return SC;
    }

    public void setScreen(String value) {
        fields.put("sc", value);
    }
}