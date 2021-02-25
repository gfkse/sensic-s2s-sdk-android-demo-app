package com.gfk.s2s.builder.eventInterface;

import java.util.Map;

public interface IEventImpression extends IEventCommon {
    String getContentId();

    Map getCustomParams();
}
