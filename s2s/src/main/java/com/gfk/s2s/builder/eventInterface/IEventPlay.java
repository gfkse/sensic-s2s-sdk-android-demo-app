package com.gfk.s2s.builder.eventInterface;

import java.util.Map;

public interface IEventPlay extends IEventBase {
    String getContentId();

    IEventPlayOptions getOptions();

    Map getCustomParams();

    long getStreamStartTime();

}
