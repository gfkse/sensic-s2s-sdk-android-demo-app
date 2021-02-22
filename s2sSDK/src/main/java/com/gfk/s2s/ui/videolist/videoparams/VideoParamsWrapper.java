package com.gfk.s2s.ui.videolist.videoparams;

import java.util.HashMap;

public class VideoParamsWrapper {

    public static final String VIDEO_PARAMS ="video_params";

    private static VideoParamsWrapper instance;
    private HashMap<String,String> videoParams = new HashMap<>();

    public static VideoParamsWrapper getInstance() {
        if(instance == null){
            instance = new VideoParamsWrapper();
        }

        return instance;
    }

    public HashMap<String, String> getVideoParams() {
        return videoParams;
    }

    public void setVideoParams(HashMap<String, String> videoParams) {
        this.videoParams = videoParams;
    }
}
