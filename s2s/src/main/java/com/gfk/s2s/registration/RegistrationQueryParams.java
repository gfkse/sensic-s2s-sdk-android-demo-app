package com.gfk.s2s.registration;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.gfk.s2s.utils.GlobalConst;

import java.net.URI;
import java.net.URISyntaxException;

public class RegistrationQueryParams {

    private String panelId;
    private String panelUserId;
    private String touchPointUrl;
    private String gotoUrl;
    private String state;
    private String suiGeneratorUrl;
    private String scheme;
    private String notifyUrl;
    private String processStateServerUrl;
    private String processId;
    private String lastApp;
    private String suiTpUrl;

    public RegistrationQueryParams(Uri uri) {
        panelId = checkForNull(uri.getQueryParameter("pid"));
        panelUserId = checkForNull(uri.getQueryParameter("paneluserid"));
        touchPointUrl = checkForNull(uri.getQueryParameter("webview"));
        gotoUrl = checkForNull(uri.getQueryParameter("goto"));
        suiGeneratorUrl = checkForNull(uri.getQueryParameter("suigenerator"));
        state = checkForNull(uri.getQueryParameter("state"));
        scheme = checkForNull(uri.getScheme());
        processStateServerUrl = checkForNull(uri.getQueryParameter("processstateserver"));
        processId = checkForNull(uri.getQueryParameter("processid"));
        lastApp = checkForNull(uri.getQueryParameter("lastapp"));
        suiTpUrl = checkForNull(uri.getQueryParameter("suitp"));
        notifyUrl = processStateServerUrl + "/" + processId + "/notify";
    }

    public String getPanelId() {
        return panelId;
    }

    public String getPanelUserId() {
        return panelUserId;
    }

    public String getTouchPointUrl() {
        return touchPointUrl;
    }

    public String getGotoUrl() {
        return gotoUrl;
    }

    public String getState() {
        return state;
    }

    public String getSuiGeneratorUrl() {
        return suiGeneratorUrl;
    }

    public String getScheme() {
        return scheme;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getProcessStateServerUrl() {
        return processStateServerUrl;
    }

    public String getProcessId() {
        return processId;
    }

    public String getLastApp() {
        return lastApp;
    }

    public String getSuiTpUrl() {
        return suiTpUrl;
    }

    public void setState(String state) {
        this.state = state;
    }

    private String checkForNull(String value) {
        return value != null ? value : "";
    }

    public String getWebViewUrl() {
        URI uri;
        try {
            uri = appendUri(touchPointUrl, "pid=" + panelId);
            uri = appendUri(uri.toString(), "lastapp=" + lastApp);
            uri = appendUri(uri.toString(), "paneluserid=" + panelUserId);
            uri = appendUri(uri.toString(), "webview=" + touchPointUrl);
            uri = appendUri(uri.toString(), "state=" + state);
            uri = appendUri(uri.toString(), "processstateserver=" + processStateServerUrl);
            uri = appendUri(uri.toString(), "processid=" + processId);
            uri = appendUri(uri.toString(), "suigenerator=" + suiGeneratorUrl);
            uri = appendUri(uri.toString(), "goto=" + gotoUrl);
            uri = appendUri(uri.toString(), "suitp=" + suiTpUrl);

            return uri.toString();

        } catch (URISyntaxException e) {
           Log.e(GlobalConst.LOG_TAG, "Exception while adding query parameter in getWebViewUrl()");
        }

        return "";
    }

    public String getEnrichedSuiGeneratorUrl(Context context) {
        URI uri;
        try {
            uri = appendUri(suiGeneratorUrl, "ai=" + RegistrationUtils.getAdvertisingId(context));
            uri = appendUri(uri.toString(), "pid=" + panelId);
            uri = appendUri(uri.toString(), "paneluserid=" + panelUserId);
            uri = appendUri(uri.toString(), "f=json");

            return uri.toString();

        } catch (URISyntaxException e) {
            Log.e(GlobalConst.LOG_TAG, "Exception while adding query parameter in getWebViewUrl()");
        }

        return "";
    }

    static URI appendUri(String uri, String appendQuery) throws URISyntaxException {
        URI oldUri = new URI(uri);
        return new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(),
                oldUri.getQuery() == null ? appendQuery : oldUri.getQuery() + "&" + appendQuery, oldUri.getFragment());
    }
}
