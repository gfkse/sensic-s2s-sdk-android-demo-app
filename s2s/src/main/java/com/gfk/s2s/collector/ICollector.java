package com.gfk.s2s.collector;

import java.util.List;

public interface ICollector {
    SegmentConfig getSegmentConfig();
    String getTrackingUrl();
    String getSuiUrl();
    String getLanguage();
    String getUserAgent();
    String getTechnology();
    String getVersion();
    String getOrigin();
    String getAdvertisingId();
    String getProjectName();
    String getAppType();
    String getSui();
    String getDeviceType();
    List<String> getStreamCustomParameter();
    List<String> getContentCustomParameter();
}
