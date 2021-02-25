package com.gfk.s2s.builder.eventInterface;

public interface IEventBase extends IEventCommon {

    int getStreamPosition();

    String getPresentationId();

    int getSegmentStateItemNumber();

    int getSegmentNumber();

    int getSegmentDuration();

    long getUsageTime();
}
