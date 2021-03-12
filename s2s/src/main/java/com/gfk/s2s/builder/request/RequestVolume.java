package com.gfk.s2s.builder.request;

public class RequestVolume extends RequestBase {

    private static final String VO = "VO";

    public String getRequestType() {
        return VO;
    }

    public void setVolume(String value) {
        fields.put("vo", value);
    }
}
