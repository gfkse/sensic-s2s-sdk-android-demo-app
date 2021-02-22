package com.gfk.s2s.builder.segment;

public interface ISegment {

    long getStreamPosition();
    String getPresentationId();
    int getSegmentNumber();
    int getStateItemNumber();
    int getSegmentDuration();
}
