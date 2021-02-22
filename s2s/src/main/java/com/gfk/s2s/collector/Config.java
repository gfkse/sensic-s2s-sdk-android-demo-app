package com.gfk.s2s.collector;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Config {

    private Boolean dnt = null;
    private String suiGeneratorUrl = "";

    private Boolean enabled = null;
    private String projectVersion = "";
    private String configVersion = "";
    private String tech = "";
    private String projectName = "";
    private String trackingUrl = "";
    private List<String> streamCustom = new ArrayList<>();

    private List<String> contentCustom = new ArrayList<>();
    private SegmentConfig segmentConfig;
    private String tsUrl = "";

    public static Config createFromJson(@NonNull String json) {
        if (TextUtils.isEmpty(json)) {
            return new Config();
        }

        Config config = new Config();

        try {
            JSONObject jsonObject = new JSONObject(json);
            config.enabled = jsonObject.getBoolean("enabled");
            config.projectVersion = jsonObject.getString("projectVersion");
            config.projectName = jsonObject.getString("projectName");
            config.configVersion = jsonObject.getString("configVersion");
            config.tech = jsonObject.getString("tech");
            config.trackingUrl = jsonObject.getString("trackingUrl");
            config.tsUrl = jsonObject.getString("tsUrl");
            config.dnt = jsonObject.getBoolean("dnt");
            config.suiGeneratorUrl= jsonObject.getString("suiGeneratorUrl");

            JSONObject segmentObject = jsonObject.getJSONObject("segment");

            int minSegmentDuration = segmentObject.getInt("minSegmentDuration");
            int maxStateItemsNumber = segmentObject.getInt("maxSegmentStateItems");
            int maxSegmentNumber = segmentObject.getInt("maxSegments");

            config.segmentConfig = new SegmentConfig(minSegmentDuration, maxStateItemsNumber, maxSegmentNumber);

            JSONArray jsonArray = jsonObject.getJSONArray("streamCustom");
            config.streamCustom = new ArrayList<String>();
            for (int i = 0; i < jsonArray.length(); i++) {
                config.streamCustom.add(jsonArray.getString(i));
            }

            jsonArray = jsonObject.getJSONArray("contentCustom");
            config.contentCustom = new ArrayList<String>();
            for (int i = 0; i < jsonArray.length(); i++) {
                config.contentCustom.add(jsonArray.getString(i));
            }


            return config;

        } catch (JSONException e) {
            return new Config();
        }
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public String getTech() {
        return tech;
    }

    public void setTech(String tech) {
        this.tech = tech;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public Boolean isDnt() {
        return dnt;
    }

    public void setDnt(Boolean dnt) {
        this.dnt = dnt;
    }

    public String getSuiGeneratorUrl() {
        return suiGeneratorUrl;
    }

    public void setSuiGeneratorUrl(String suiGeneratorUrl) {
        this.suiGeneratorUrl = suiGeneratorUrl;
    }

    public List<String> getStreamCustom() {
        return streamCustom;
    }

    public void setStreamCustom(ArrayList<String> streamCustom) {
        this.streamCustom = streamCustom;
    }

    public SegmentConfig getSegmentConfig() {
        return segmentConfig;
    }

    public List<String> getContentCustom() {
        return contentCustom;
    }

    public void setContentCustom(ArrayList<String> contentCustom) {
        this.contentCustom = contentCustom;
    }

    public String getTsUrl() {
        return tsUrl;
    }
}
