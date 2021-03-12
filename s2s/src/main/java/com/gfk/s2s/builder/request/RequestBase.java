package com.gfk.s2s.builder.request;

public abstract class RequestBase extends RequestCommon {

    public void setPresentationId(String value) {
        fields.put("pr", value);
    }

    public void setRequestNumber(String value) {
        fields.put("rn", value);
    }

    public void setSegmentNumber(String value) {
        fields.put("sn", value);
    }

    public void setStreamPosition(int value) {
        Integer vp = value == -1 ? value : (int)Math.round((double)value / 1000);
        fields.put("sp", vp.toString());

        // @Deprecated - will be removed in future version
        fields.put("vp", vp.toString());
    }

    public void setSegmentDuration(int value) {
        int rounded = (int)Math.round((double)value / 1000);
        fields.put("sd", "" + rounded);

        // @Deprecated - will be removed in future version
        fields.put("vt", "" + rounded);
    }

    public void setUsageTime(long value) {
        fields.put("ut", ""+value / 1000);
    }

    public void setStreamStartTime(long value) {
        fields.put("st", ""+value / 1000);
        // @Deprecated - will be removed in future version
        fields.put("ct", ""+value / 1000);
    }
}
