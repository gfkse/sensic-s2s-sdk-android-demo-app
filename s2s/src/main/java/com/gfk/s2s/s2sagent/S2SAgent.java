package com.gfk.s2s.s2sagent;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gfk.s2s.builder.eventInterface.IEventPlayOptions;
import com.gfk.s2s.processor.Processor;
import com.gfk.s2s.utils.GlobalConst;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public final class S2SAgent {

    private static final String CONTENT_ID = "contentId";
    private static final String STREAM_ID = "streamId";
    private static final String STREAM_START_TIME = "streamStartTime";
    private static final String OPTION_SCREEN = "screen";
    private static final String OPTION_VOLUME = "volume";
    private static final String OPTION_DEVICETYPE = "deviceType";

    private Processor processor;

    void setProcessor(Processor processor) {
        this.processor = processor;
    }

    /**
     * To be used for LIVE player.
     * Therefore, this constructor must be used in conjunction with playStreamLive() only.
     */
    public S2SAgent(@NonNull String configUrl, @NonNull String mediaId, Boolean optin, Context context) {
        this(configUrl, mediaId, optin, null, context);
    }

    /**
     * To be used for LIVE player.
     * Therefore, this constructor must be used in conjunction with playStreamLive() only.
     */
    public S2SAgent(@NonNull String configUrl, @NonNull String mediaId, Context context) {
        this(configUrl, mediaId, true, null, context);
    }

    /**
     * To be used for VOD player.
     * Therefore, this constructor must be used in conjunction with playStreamOnDemand() only.
     */
    public S2SAgent(@NonNull String configUrl, @NonNull String mediaId, StreamPositionCallback streamPositionCallback, Context context) {
        this(configUrl, mediaId, true, streamPositionCallback, context);
    }

    /**
     * To be used for VOD player.
     * Therefore, this constructor must be used in conjunction with playStreamOnDemand() only.
     */
    public S2SAgent(@NonNull String configUrl, @NonNull String mediaId, Boolean optin, StreamPositionCallback streamPositionCallback, Context context) {
        if (TextUtils.isEmpty(configUrl)) {
            throw new IllegalArgumentException("S2SAgent must be initialized with an URL to the configuration file, but the provided configUrl was null");
        }
        if (TextUtils.isEmpty(mediaId)) {
            throw new IllegalArgumentException("S2SAgent must be initialized with a mediaId, but the provided mediaId was null");
        }
        if (context == null) {
            throw new IllegalArgumentException("S2SAgent must be initialized with a context, but the provided context was null");
        }

        Log.d("GfKlog", "S2SAgent successfully instantiated");
        processor = new Processor(configUrl, mediaId, optin, streamPositionCallback, context);
    }

    public void setStreamPositionCallback(StreamPositionCallback streamPositionCallback) {
        processor.setStreamPositionCallback(streamPositionCallback);
    }

    @Deprecated
    public S2SAgent(@NonNull String configUrl, @NonNull String mediaId, VideoPositionCallback videoPositionCallback, Context context) {
        this(configUrl, mediaId, true, videoPositionCallback, context);
    }

    public HashMap impression(@NonNull String contentId, Map customParams) {
        HashMap<String, Boolean> errors = new HashMap<>();
        if (TextUtils.isEmpty(contentId)) {
            Log.e(GlobalConst.LOG_TAG, "Error in impression: parameter \"contentId\" is empty.");
            errors.put(CONTENT_ID, Boolean.FALSE);
        }

        processor.createEventImpression(contentId, customParams);

        Log.d(GlobalConst.LOG_TAG, "agent.impression(" + contentId + ", " + customParams + ") is called");

        return errors;
    }

    @Deprecated
    public HashMap playVOD(@NonNull String contentId, String videoId, String screen, String volume, Map customParams) {
        HashMap<String, String> options = new HashMap<>();
        options.put(OPTION_SCREEN, screen);
        options.put(OPTION_VOLUME, volume);

        return playStreamOnDemand(contentId, videoId, options, customParams);
    }

    public HashMap playStreamOnDemand(@NonNull String contentId, String streamId, Map customParams) {
        HashMap<String, String> options = new HashMap<>();
        return playStreamOnDemand(contentId, streamId, options, customParams);
    }

    public HashMap playStreamOnDemand(@NonNull String contentId, String streamId, Map<String, String> options, Map customParams) {
        HashMap<String, Boolean> errors = new HashMap<>();

        if (TextUtils.isEmpty(contentId)) {
            Log.e(GlobalConst.LOG_TAG, "Error in playStreamOnDemand: parameter \"contentId\" is empty.");
            errors.put(CONTENT_ID, Boolean.FALSE);
        }

        if (TextUtils.isEmpty(streamId)) {
            Log.e(GlobalConst.LOG_TAG, "Error in playStreamOnDemand: parameter \"streamId\" is empty.");
            errors.put(STREAM_ID, Boolean.FALSE);
        }

        final String screen;
        if (options.containsKey(OPTION_SCREEN) && (options.get(OPTION_SCREEN) == null || options.get(OPTION_SCREEN).length() == 0)) {
            screen = "";
        } else {
            screen = options.get(OPTION_SCREEN) == null ? "" : options.get(OPTION_SCREEN);
        }

        final String volume;
        if (!options.containsKey(OPTION_VOLUME) || (options.containsKey(OPTION_VOLUME) && (options.get(OPTION_VOLUME) == null || options.get(OPTION_VOLUME).length() == 0))) {
            volume = "";
        } else {
            volume = options.get(OPTION_VOLUME);
        }

        final String deviceType = options.get(OPTION_DEVICETYPE) == null ? "" : options.get(OPTION_DEVICETYPE);

        if (areParameterValid(errors)) {
            IEventPlayOptions playOptions = new IEventPlayOptions() {
                @Override
                public String getVolume() {
                    return volume;
                }

                @Override
                public String getScreen() {
                    return screen;
                }

                @Override
                public String getDeviceType() {
                    return deviceType;
                }
            };

            Log.d(GlobalConst.LOG_TAG, "agent.playStreamOnDemand(" + contentId
                    + ", " + (!TextUtils.isEmpty(streamId) ? streamId : "streamId not set")
                    + ", " + "(playOptions: " + (!TextUtils.isEmpty(playOptions.getDeviceType()) ? playOptions.getDeviceType() : "DeviceType not set") + ", "
                    + (!TextUtils.isEmpty(playOptions.getScreen()) ? playOptions.getScreen() : "Screen not set") + ", "
                    + (!TextUtils.isEmpty(playOptions.getVolume()) ? playOptions.getVolume() : "Volume not set") + ")"
                    + ", " + "customParams: " + customParams
                    + ") is called");
            processor.createEventPlayOnDemand(contentId, streamId, playOptions, customParams, null);
        }

        return errors;
    }

    @Deprecated
    public HashMap playLive(@NonNull String contentId, String streamStart, int videoOffset, String videoId, String screen, String volume, Map customParams) {
        HashMap<String, String> options = new HashMap<>();
        options.put(OPTION_SCREEN, screen);
        options.put(OPTION_VOLUME, volume);

        return this.playStreamLive(contentId, streamStart, videoOffset, videoId, options, customParams);
    }

    public HashMap playStreamLive(@NonNull String contentId, String streamStart, int streamOffset, String streamId, Map customParams) {
        HashMap<String, String> options = new HashMap<>();
        return playStreamLive(contentId, streamStart, streamOffset, streamId, options, customParams);
    }

    public HashMap playStreamLive(@NonNull String contentId, String streamStart, int streamOffset, String streamId, final Map<String, String> options, Map customParams) {
        HashMap<String, Boolean> errors = new HashMap<>();

        if (TextUtils.isEmpty(contentId)) {
            Log.e(GlobalConst.LOG_TAG, "Error in playStreamLive: parameter \"contentId\" is empty.");
            errors.put(CONTENT_ID, Boolean.FALSE);
        }

        if (TextUtils.isEmpty(streamId)) {
            Log.e(GlobalConst.LOG_TAG, "Error in playStreamLive: parameter \"streamId\" is empty.");
            errors.put(STREAM_ID, Boolean.FALSE);
        }

        final String screen;
        if (options.containsKey(OPTION_SCREEN) && (options.get(OPTION_SCREEN) == null || options.get(OPTION_SCREEN).length() == 0)) {
            screen = "";
        } else {
            screen = options.get(OPTION_SCREEN) == null ? "" : options.get(OPTION_SCREEN);
        }

        final String volume;
        if (!options.containsKey(OPTION_VOLUME) || (options.containsKey(OPTION_VOLUME) && (options.get(OPTION_VOLUME) == null || options.get(OPTION_VOLUME).length() == 0))) {
            volume = "";
        } else {
            volume = options.get(OPTION_VOLUME);
        }

        if (!isCorrectStreamStart(streamStart)) {
            Log.e(GlobalConst.LOG_TAG, "Error in playStreamLive: parameter \"streamStart\" is invalid.");
            errors.put(STREAM_START_TIME, Boolean.FALSE);
        }

        final String deviceType = options.get(OPTION_DEVICETYPE) == null ? "" : options.get(OPTION_DEVICETYPE);

        if (areParameterValid(errors)) {
            IEventPlayOptions playOptions = new IEventPlayOptions() {
                @Override
                public String getVolume() {
                    return volume;
                }

                @Override
                public String getScreen() {
                    return screen;
                }

                @Override
                public String getDeviceType() {
                    return deviceType;
                }
            };

            Log.d(GlobalConst.LOG_TAG, "agent.playStreamLive(" + contentId
                    + ", " + (!TextUtils.isEmpty(streamStart) ? streamStart : "streamStart not set")
                    + ", " + (!TextUtils.isEmpty(String.valueOf(streamOffset)) ? String.valueOf(streamOffset) : "streamOffset not set")
                    + ", " + (!TextUtils.isEmpty(streamId) ? streamId : "streamId not set")
                    + ", " + "(playOptions: " + (!TextUtils.isEmpty(playOptions.getDeviceType()) ? playOptions.getDeviceType() : "DeviceType not set") + ", "
                    + (!TextUtils.isEmpty(playOptions.getScreen()) ? playOptions.getScreen() : "Screen not set") + ", "
                    + (!TextUtils.isEmpty(playOptions.getVolume()) ? playOptions.getVolume() : "Volume not set") + ")"
                    + ", " + "customParams: " + customParams
                    + ") is called");
            processor.createEventPlayLive(contentId, streamStart, streamOffset, streamId, playOptions, customParams, null);
        }

        return errors;
    }


    public HashMap stop() {
        return stop(null);
    }

    public HashMap stop(Long bufferedStreamPosition) {
        HashMap<String, Boolean> errors = new HashMap<>();
        Log.d(GlobalConst.LOG_TAG, "agent.stop() is called");
        processor.createEventStop(bufferedStreamPosition);
        return errors;
    }

    public HashMap skip() {
        HashMap<String, Boolean> errors = new HashMap<>();
        Log.d(GlobalConst.LOG_TAG, "agent.skip() is called");
        processor.createEventSkip(null);
        return errors;
    }

    public HashMap volume(String volume) {
        HashMap<String, Boolean> errors = new HashMap<>();
        if (TextUtils.isEmpty(volume)) {
            Log.e(GlobalConst.LOG_TAG, "Error in volume: parameter \"volume\" is empty.");
            errors.put(OPTION_VOLUME, Boolean.FALSE);
        }
        Log.d(GlobalConst.LOG_TAG, "agent.volume(" + volume + ") is called");
        processor.createEventVolume(volume, null);
        return errors;
    }

    public void flushEventStorage() {
        Log.d(GlobalConst.LOG_TAG, "agent.flushStorageQueue() is called");
        processor.flushStorageQueue();
    }

    public HashMap screen(String screen) {
        HashMap<String, Boolean> errors = new HashMap<>();
        if (TextUtils.isEmpty(screen)) {
            Log.e(GlobalConst.LOG_TAG, "Error in screen: parameter \"screen\" is empty.");
            errors.put(OPTION_SCREEN, Boolean.FALSE);
        }
        Log.d(GlobalConst.LOG_TAG, "agent.screen(" + screen + ") is called");
        processor.createEventScreen(screen, null);
        return errors;
    }

    private boolean areParameterValid(Map parameter) {
        for (Object value : parameter.values()) {
            if (value.equals(false)) {
                return false;
            }
        }

        return true;
    }

    private boolean isCorrectStreamStart(String value) {
        if (value == null) return false;
        if (value.length() > 0) {
            SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            dateFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                dateFormatUtc.parse(value).getTime();
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

}