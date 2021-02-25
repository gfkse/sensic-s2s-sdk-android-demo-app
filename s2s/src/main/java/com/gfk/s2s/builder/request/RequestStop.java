package com.gfk.s2s.builder.request;

public class RequestStop extends RequestBase {

    private static final String ST = "ST";

    public String getRequestType() {
        return ST;
    }

    public void setSkip(String value) {
        fields.put("sk", value);
    }
}
