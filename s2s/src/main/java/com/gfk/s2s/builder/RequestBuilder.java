package com.gfk.s2s.builder;

import com.gfk.s2s.builder.event.EventImpression;
import com.gfk.s2s.builder.event.EventPlay;
import com.gfk.s2s.builder.event.EventScreen;
import com.gfk.s2s.builder.event.EventSkip;
import com.gfk.s2s.builder.event.EventStop;
import com.gfk.s2s.builder.event.EventVolume;
import com.gfk.s2s.builder.eventInterface.IEventBase;
import com.gfk.s2s.builder.eventInterface.IEventCommon;
import com.gfk.s2s.builder.request.IRequest;
import com.gfk.s2s.builder.request.RequestBase;
import com.gfk.s2s.builder.request.RequestCommon;
import com.gfk.s2s.builder.request.RequestImpression;
import com.gfk.s2s.builder.request.RequestPlay;
import com.gfk.s2s.builder.request.RequestScreen;
import com.gfk.s2s.builder.request.RequestStop;
import com.gfk.s2s.builder.request.RequestVolume;
import com.gfk.s2s.collector.ICollector;

import java.util.HashMap;

import static com.gfk.s2s.collector.Collector.OPERATING_SYSTEM;

public class RequestBuilder {

    private ICollector collector;

    RequestBuilder(ICollector collector) {
        this.collector = collector;
    }

    public IRequest buildRequest(IEventCommon event) {
        IRequest request;

        if ( event instanceof EventPlay ) {
            request = this.buildRequestPlay((EventPlay) event);
        } else if ( event instanceof EventStop ) {
            request = this.buildRequestStop((EventStop) event);
        } else if ( event instanceof EventSkip ) {
            request = this.buildRequestSkip((EventSkip) event);
        } else if ( event instanceof EventVolume ) {
            request = this.buildRequestVolume((EventVolume) event);
        } else if ( event instanceof EventScreen ) {
            request = this.buildRequestScreen((EventScreen) event);
        } else if ( event instanceof EventImpression) {
            request = this.buildRequestImpression((EventImpression) event);
        } else {
            request = null;
        }

        return request;
    }

    IRequest buildRequestImpression(EventImpression event) {
        RequestImpression request = (RequestImpression) this.buildRequestCommon(new RequestImpression(), event);
        request.setContentId(event.getContentId());
        request.setSui(this.collector.getSui());
        request.setUserAgent(this.collector.getUserAgent());
        request.setLanguage(this.collector.getLanguage());
        request.setOrigin(this.collector.getOrigin());

        HashMap params = new HashMap();
        //noinspection unchecked
        params.put("userParams", event.getCustomParams());
        //noinspection unchecked
        params.put("allowedParams", this.collector.getContentCustomParameter());
        request.setCustomParameter(params);

        return request;
    }

    IRequest buildRequestPlay(EventPlay event) {
        RequestPlay request = (RequestPlay) this.buildRequestBase(new RequestPlay(), event);
        request.setContentId(event.getContentId());
        request.setVolume(event.getOptions().getVolume().isEmpty() ? "" : event.getOptions().getVolume());
        request.setScreen(event.getOptions().getScreen().isEmpty() ? "" : event.getOptions().getScreen());
        request.setSui(this.collector.getSui());
        request.setUserAgent(this.collector.getUserAgent());
        request.setLanguage(this.collector.getLanguage());
        request.setOrigin(this.collector.getOrigin());
        request.setStreamPosition(event.getStreamPosition());
        request.setStreamStartTime(event.getStreamStartTime());

        HashMap params = new HashMap();
        //noinspection unchecked
        params.put("userParams", event.getCustomParams());
        //noinspection unchecked
        params.put("allowedParams", this.collector.getStreamCustomParameter());
        request.setCustomParameter(params);

        return request;
    }

    IRequest buildRequestStop(EventStop event) {
        RequestStop request = (RequestStop) this.buildRequestBase(new RequestStop(), event);
        request.setSkip("0");

        return request;
    }

    IRequest buildRequestSkip(EventSkip event) {
        RequestStop request = (RequestStop) this.buildRequestBase(new RequestStop(), event);
        request.setSkip("1");

        return request;
    }

    IRequest buildRequestVolume(EventVolume event) {
        RequestVolume request = (RequestVolume) this.buildRequestBase(new RequestVolume(), event);
        request.setVolume(event.getVolume());

        return request;
    }

    IRequest buildRequestScreen(EventScreen event) {
        RequestScreen request = (RequestScreen) this.buildRequestBase(new RequestScreen(), event);
        request.setScreen(event.getScreen());

        return request;
    }

    private RequestBase buildRequestBase(RequestBase requestBase, IEventBase event) {
        RequestBase request = (RequestBase) buildRequestCommon(requestBase, event);
        request.setPresentationId(event.getPresentationId());
        request.setRequestNumber("" + event.getSegmentStateItemNumber());
        request.setSegmentNumber("" + event.getSegmentNumber());
        request.setSegmentDuration(event.getSegmentDuration());
        request.setUsageTime(event.getUsageTime());

        return request;
    }

    private RequestCommon buildRequestCommon(RequestCommon request, IEventCommon event) {
        request.setProjectId(collector.getProjectName());
        request.setMediaId(event.getMediaId());
        request.setTechnology(this.collector.getTechnology());
        request.setVersion(this.collector.getVersion());
        request.setOperatingSystem(OPERATING_SYSTEM);
        request.setDeviceType(this.collector.getDeviceType());
        request.setAppType(this.collector.getAppType());

        return request;
    }
}
