package com.gfk.s2s.builder.request;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibTestRunner.class)
public class RequestsTest {

    private int segmentDuration = 120;
    private String presentationId = "123abc";
    private String requestNumber = "4";
    private String segmentNumber = "2";
    private String mediaId = "media";
    private String operatingSystem = "macOS";
    private String technology = "ssa-a";
    private String version = "1.0/1.2.3/1";
    private String appType = "ANDROID";
    private String screen = "Fullscreen";
    private String volume = "mute";
    private int streamPosition = 6000;

    public int convertToSeconds(int time) {
        return Math.round(time / 1000);
    }

    @Test
    public void testIfRequestImpressionHasCorrectTypeAndProvideGetAsUrlStringMethod(){
        String type = "IM";
        String contentId = "content";
        String language = "de";
        String origin = "http://www.nothing.org";
        String projectId = "demo1";
        String userAgent = "Browser XXX";
        String sui = "{\"id\":123}";
        final HashMap<String, String> userParams = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "cba");}};
        final ArrayList<String> allowedParams = new ArrayList<String>(Arrays.asList("cp1"));
        HashMap<String, Object> customParameter = new HashMap<String, Object>(){{put("userParams", userParams); put("allowedParams", allowedParams);}};

        RequestImpression base = new RequestImpression();
        base.setMediaId(mediaId);
        base.setContentId(contentId);
        base.setLanguage(language);
        base.setOrigin(origin);
        base.setProjectId(projectId);
        base.setUserAgent(userAgent);
        base.setSui(sui);
        base.setOperatingSystem(operatingSystem);
        base.setTechnology(technology);
        base.setVersion(version);
        base.setAppType(appType);
        base.setCustomParameter(customParameter);

        assertThat(base.getRequestType()).isEqualTo(type);
        String urlString = base.getAsUrlString();
        assertThat(urlString).containsSequence("ty=" + type);
        assertThat(urlString).containsSequence("m=" + Uri.encode(mediaId));
        assertThat(urlString).containsSequence("os=" + Uri.encode(operatingSystem));
        assertThat(urlString).containsSequence("t=" + technology);
        assertThat(urlString).containsSequence("v=" + Uri.encode(version));
        assertThat(urlString).containsSequence("at=" + appType);
        assertThat(urlString).containsSequence("cp1=" + Uri.encode(userParams.get("cp1")));
        assertThat(urlString).doesNotContain("cp2=" + Uri.encode(userParams.get("cp2")));
        assertThat(urlString).containsSequence("c=" + Uri.encode(contentId));
        assertThat(urlString).containsSequence("l=" + language);
        assertThat(urlString).containsSequence("r=" + Uri.encode(origin));
        assertThat(urlString).containsSequence("p=" + projectId);
        assertThat(urlString).containsSequence("ua=" + Uri.encode(userAgent));
        assertThat(urlString).containsSequence("sui=" + Uri.encode(sui));
    }

    @Test
    public void testIfRequestPlayHasCorrectTypeAndProvideGetAsUrlStringMethod(){
        String type = "PL";
        String contentId = "content";
        String language = "de";
        String origin = "http://www.nothing.org";
        String projectId = "demo1";
        String userAgent = "Browser XXX";
        String sui = "{\"id\":123}";
        final HashMap<String, String> userParams = new HashMap<String, String>(){{put("cp1", "abc"); put("cp2", "cba");}};
        final ArrayList<String> allowedParams = new ArrayList<String>(Arrays.asList("cp1"));
        HashMap<String, Object> customParameter = new HashMap<String, Object>(){{put("userParams", userParams); put("allowedParams", allowedParams);}};

        RequestPlay base = new RequestPlay();
        base.setSegmentDuration(segmentDuration);
        base.setPresentationId(presentationId);
        base.setRequestNumber(requestNumber);
        base.setSegmentNumber(segmentNumber);
        base.setMediaId(mediaId);
        base.setContentId(contentId);
        base.setStreamPosition(streamPosition);
        base.setScreen(screen);
        base.setVolume(volume);
        base.setLanguage(language);
        base.setOrigin(origin);
        base.setProjectId(projectId);
        base.setUserAgent(userAgent);
        base.setSui(sui);
        base.setOperatingSystem(operatingSystem);
        base.setTechnology(technology);
        base.setVersion(version);
        base.setAppType(appType);
        base.setCustomParameter(customParameter);

        assertThat(base.getRequestType()).isEqualTo(type);
        String urlString = base.getAsUrlString();
        assertThat(urlString).containsSequence("ty=" + type);
        assertThat(urlString).containsSequence("sd=" + convertToSeconds(segmentDuration));
        // @deprecated
        assertThat(urlString).containsSequence("vt=" + convertToSeconds(segmentDuration));
        assertThat(urlString).containsSequence("pr=" + Uri.encode(presentationId));
        assertThat(urlString).containsSequence("rn=" + requestNumber);
        assertThat(urlString).containsSequence("sn=" + segmentNumber);
        assertThat(urlString).containsSequence("m=" + Uri.encode(mediaId));
        assertThat(urlString).containsSequence("os=" + Uri.encode(operatingSystem));
        assertThat(urlString).containsSequence("t=" + technology);
        assertThat(urlString).containsSequence("v=" + Uri.encode(version));
        assertThat(urlString).containsSequence("at=" + appType);
        assertThat(urlString).containsSequence("cp1=" + Uri.encode(userParams.get("cp1")));
        assertThat(urlString).doesNotContain("cp2=" + Uri.encode(userParams.get("cp2")));
        assertThat(urlString).containsSequence("c=" + Uri.encode(contentId));
        // @deprecated
        assertThat(urlString).containsSequence("vp=" + convertToSeconds(streamPosition));
        assertThat(urlString).containsSequence("sp=" + convertToSeconds(streamPosition));
        assertThat(urlString).containsSequence("sc=" + Uri.encode(screen));
        assertThat(urlString).containsSequence("vo=" + Uri.encode(volume));
        assertThat(urlString).containsSequence("l=" + language);
        assertThat(urlString).containsSequence("r=" + Uri.encode(origin));
        assertThat(urlString).containsSequence("p=" + projectId);
        assertThat(urlString).containsSequence("ua=" + Uri.encode(userAgent));
        assertThat(urlString).containsSequence("sui=" + Uri.encode(sui));
    }

    @Test
    public void testIfRequestStopHasCorrectTypeAndProvidesGetAsUrlStringMethod() {

        String type = "ST";
        String skip = "1";

        RequestStop base = new RequestStop();
        base.setSkip(skip);
        base.setPresentationId(presentationId);
        base.setRequestNumber(requestNumber);
        base.setSegmentNumber(segmentNumber);
        base.setMediaId(mediaId);
        base.setOperatingSystem(operatingSystem);
        base.setTechnology(technology);
        base.setSegmentDuration(segmentDuration);
        base.setVersion(version);
        base.setAppType(appType);

        assertThat(base.getRequestType()).isEqualTo(type);
        String urlString = base.getAsUrlString();
        assertThat(urlString).containsSequence("ty=" + type);
        assertThat(urlString).containsSequence("sk=" + skip);
        // @deprecated
        assertThat(urlString).containsSequence("vt=" + convertToSeconds(segmentDuration));
        assertThat(urlString).containsSequence("sd=" + convertToSeconds(segmentDuration));
        assertThat(urlString).containsSequence("pr=" + Uri.encode(presentationId));
        assertThat(urlString).containsSequence("rn=" + requestNumber);
        assertThat(urlString).containsSequence("sn=" + segmentNumber);
        assertThat(urlString).containsSequence("m=" + Uri.encode(mediaId));
        assertThat(urlString).containsSequence("os=" + Uri.encode(operatingSystem));
        assertThat(urlString).containsSequence("t=" + technology);
        assertThat(urlString).containsSequence("v=" + Uri.encode(version));
        assertThat(urlString).containsSequence("at=" + appType);
    }

    @Test
    public void testIfRequestScreenHasCorrectTypeAndProvidesGetAsUrlStringMethod() {

        String type = "SC";

        RequestScreen base = new RequestScreen();
        base.setPresentationId(presentationId);
        base.setRequestNumber(requestNumber);
        base.setSegmentNumber(segmentNumber);
        base.setMediaId(mediaId);
        base.setOperatingSystem(operatingSystem);
        base.setTechnology(technology);
        base.setSegmentDuration(segmentDuration);
        base.setVersion(version);
        base.setAppType(appType);
        base.setScreen(screen);
        base.setStreamPosition(streamPosition);

        assertThat(base.getRequestType()).isEqualTo(type);
        String urlString = base.getAsUrlString();
        assertThat(urlString).containsSequence("ty=" + type);
        assertThat(urlString).containsSequence("sd=" + convertToSeconds(segmentDuration));
        // @deprecated
        assertThat(urlString).containsSequence("vt=" + convertToSeconds(segmentDuration));
        assertThat(urlString).containsSequence("pr=" + Uri.encode(presentationId));
        assertThat(urlString).containsSequence("rn=" + requestNumber);
        assertThat(urlString).containsSequence("sn=" + segmentNumber);
        assertThat(urlString).containsSequence("m=" + Uri.encode(mediaId));
        assertThat(urlString).containsSequence("os=" + Uri.encode(operatingSystem));
        assertThat(urlString).containsSequence("t=" + technology);
        assertThat(urlString).containsSequence("v=" + Uri.encode(version));
        assertThat(urlString).containsSequence("at=" + appType);
        assertThat(urlString).containsSequence("sc=" + Uri.encode(screen));
        assertThat(urlString).containsSequence("vp=" + convertToSeconds(streamPosition));
        assertThat(urlString).containsSequence("sp=" + convertToSeconds(streamPosition));
    }

    @Test
    public void testIfRequestVolumeHasCorrectTypeAndProvidesGetAsUrlStringMethod() {

        String type = "VO";

        RequestVolume base = new RequestVolume();
        base.setPresentationId(presentationId);
        base.setRequestNumber(requestNumber);
        base.setSegmentNumber(segmentNumber);
        base.setMediaId(mediaId);
        base.setOperatingSystem(operatingSystem);
        base.setTechnology(technology);
        base.setSegmentDuration(segmentDuration);
        base.setVersion(version);
        base.setAppType(appType);
        base.setVolume(volume);
        base.setStreamPosition(streamPosition);

        assertThat(base.getRequestType()).isEqualTo(type);
        String urlString = base.getAsUrlString();
        assertThat(urlString).containsSequence("ty=" + type);
        assertThat(urlString).containsSequence("sd=" + convertToSeconds(segmentDuration));
        // @deprecated
        assertThat(urlString).containsSequence("vt=" + convertToSeconds(segmentDuration));
        assertThat(urlString).containsSequence("pr=" + Uri.encode(presentationId));
        assertThat(urlString).containsSequence("rn=" + requestNumber);
        assertThat(urlString).containsSequence("sn=" + segmentNumber);
        assertThat(urlString).containsSequence("m=" + Uri.encode(mediaId));
        assertThat(urlString).containsSequence("os=" + Uri.encode(operatingSystem));
        assertThat(urlString).containsSequence("t=" + technology);
        assertThat(urlString).containsSequence("v=" + Uri.encode(version));
        assertThat(urlString).containsSequence("at=" + appType);
        assertThat(urlString).containsSequence("vo=" + Uri.encode(volume));
        assertThat(urlString).containsSequence("vp=" + convertToSeconds(streamPosition));
        assertThat(urlString).containsSequence("sp=" + convertToSeconds(streamPosition));
    }
}