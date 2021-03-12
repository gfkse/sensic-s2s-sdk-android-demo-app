package com.gfk.s2s.registration;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.gfk.s2s.utils.StateSchemeHelper;
import com.gfk.s2s.utils.factory.RegistrationQueryParamsFactory;
import com.gfk.s2s.utils.factory.RegistrationWebViewClientFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Objects;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({RegistrationUtils.class, Log.class, RegistrationWebViewClientFactory.class, RegistrationQueryParamsFactory.class, Objects.class, StateSchemeHelper.class})
@RunWith(PowerMockRunner.class)
public class S2SRegistrationUnitTest {

    public static final String STATE = "state";
    public static final String SCHEMA = "schema";
    public static final String JSON_STRING = "{\"key\": \"value\"}";
    public static final String TOUCH_POINT_URL = "touchPointUrl";
    public static final String GO_TO_URL = "goToUrl";
    public static final String WEB_VIEW_URL = "webVieUrl";
    public static final String NOTIFY_URL = "notifyUrl";

    @Mock
    private Context mockContext;

    @Mock
    private RegistrationQueryParams mockParams;

    @Mock
    private JSONObject mockJson;


    @Test
    public void storePanelist() throws JSONException {
        ViewGroup mockRootView = mock(ViewGroup.class);
        Uri mockUri = mock(Uri.class);

        mockStatic(RegistrationUtils.class);
        mockStatic(RegistrationQueryParamsFactory.class);
        mockStatic(RegistrationWebViewClientFactory.class);
        mockStatic(StateSchemeHelper.class);

        when(RegistrationQueryParamsFactory.build(mockUri)).thenReturn(mockParams);
        when(mockParams.getState()).thenReturn(STATE);
        when(mockParams.getScheme()).thenReturn(SCHEMA);

        when(StateSchemeHelper.findStateKeyForScheme(mockJson, SCHEMA)).thenReturn(SCHEMA);
        when(RegistrationUtils.getJsonObject(STATE)).thenReturn(mockJson);

        //handle SUI
        when(mockJson.get(SCHEMA)).thenReturn(AppState.CONNECTED.toString());
        when(mockRootView.getContext()).thenReturn(mockContext);
        //end handle SUI

        when(mockJson.toString()).thenReturn(JSON_STRING);

        WebView mockWebView = mock(WebView.class);
        when(RegistrationUtils.createWebView(mockContext)).thenReturn(mockWebView);

        when(mockParams.getTouchPointUrl()).thenReturn(TOUCH_POINT_URL);
        when(mockParams.getGotoUrl()).thenReturn(GO_TO_URL);

        RegistrationWebViewClient mockWebViewClient = mock(RegistrationWebViewClient.class);
        when(RegistrationWebViewClientFactory.build(mockContext, mockRootView)).thenReturn(mockWebViewClient);

        when(mockParams.getWebViewUrl()).thenReturn(WEB_VIEW_URL);
        when(mockParams.getNotifyUrl()).thenReturn(NOTIFY_URL);

        S2SRegistration.storePanelist(mockRootView, mockUri);

        verifyStatic();
        RegistrationUtils.changeJsonObject(mockJson, SCHEMA);

        verifyStatic();
        RegistrationUtils.loadSui(mockParams, mockContext);

        verifyStatic();
        RegistrationUtils.createWebView(mockContext);

        verifyStatic();
        RegistrationWebViewClientFactory.build(mockContext, mockRootView);

        verify(mockRootView).addView(mockWebView);
        verify(mockWebView).setWebViewClient(mockWebViewClient);
        verify(mockWebView).loadUrl(WEB_VIEW_URL);
    }

    @Test
    public void storePanelistReadingPropertyOfJson() throws JSONException {
        ViewGroup mockRootView = mock(ViewGroup.class);
        Uri mockUri = mock(Uri.class);

        mockStatic(RegistrationUtils.class);
        mockStatic(RegistrationQueryParamsFactory.class);
        mockStatic(RegistrationWebViewClientFactory.class);
        mockStatic(Log.class);
        mockStatic(StateSchemeHelper.class);

        when(RegistrationQueryParamsFactory.build(mockUri)).thenReturn(mockParams);
        when(mockParams.getState()).thenReturn(STATE);
        when(mockParams.getScheme()).thenReturn(SCHEMA);

        when(StateSchemeHelper.findStateKeyForScheme(mockJson, SCHEMA)).thenReturn(SCHEMA);
        when(RegistrationUtils.getJsonObject(STATE)).thenReturn(mockJson);

        //handle SUI
        when(mockJson.get(SCHEMA)).thenThrow(mock(JSONException.class));
        when(mockRootView.getContext()).thenReturn(mockContext);
        //end handle SUI

        WebView mockWebView = mock(WebView.class);
        when(RegistrationUtils.createWebView(mockContext)).thenReturn(mockWebView);

        when(mockParams.getTouchPointUrl()).thenReturn(TOUCH_POINT_URL);
        when(mockParams.getGotoUrl()).thenReturn(GO_TO_URL);

        RegistrationWebViewClient mockWebViewClient = mock(RegistrationWebViewClient.class);
        when(RegistrationWebViewClientFactory.build(mockContext, mockRootView)).thenReturn(mockWebViewClient);

        when(mockParams.getWebViewUrl()).thenReturn(WEB_VIEW_URL);
        when(mockParams.getNotifyUrl()).thenReturn(NOTIFY_URL);

        S2SRegistration.storePanelist(mockRootView, mockUri);

        //verifyStatic();
        //RegistrationUtils.notifyServerAboutAppStart(NOTIFY_URL);

        verifyStatic();
        RegistrationUtils.changeJsonObject(mockJson, SCHEMA);

        verifyStatic();
        Log.e("GfKlog", "JSONException could not find schema in state object. jSONException");

        verifyStatic();
        RegistrationUtils.createWebView(mockContext);

        verifyStatic();
        RegistrationWebViewClientFactory.build(mockContext, mockRootView);

        verify(mockRootView).addView(mockWebView);
        verify(mockWebView).setWebViewClient(mockWebViewClient);
        verify(mockWebView).loadUrl(WEB_VIEW_URL);
    }

    @Test
    public void handleSuiIsLoadingSui() throws Exception {
        mockStatic(RegistrationUtils.class);
        mockStatic(StateSchemeHelper.class);

        when(StateSchemeHelper.findStateKeyForScheme(mockJson, SCHEMA)).thenReturn(SCHEMA);
        when(mockParams.getScheme()).thenReturn(SCHEMA);
        when(mockJson.get(SCHEMA)).thenReturn(AppState.CONNECTED.toString());

        Whitebox.invokeMethod(new S2SRegistration(), "handleSUI", mockContext, mockParams, mockJson);

        verifyStatic();
        RegistrationUtils.loadSui(mockParams, mockContext);
    }

    @Test
    public void handleSuiIsRemovingSui() throws Exception {
        mockStatic(RegistrationUtils.class);
        mockStatic(StateSchemeHelper.class);
        when(mockParams.getScheme()).thenReturn(SCHEMA);
        when(mockJson.get(SCHEMA)).thenReturn(AppState.DISCONNECTED.toString());
        when(StateSchemeHelper.findStateKeyForScheme(mockJson, SCHEMA)).thenReturn(SCHEMA);

        Whitebox.invokeMethod(new S2SRegistration(), "handleSUI", mockContext, mockParams, mockJson);

        verifyStatic();
        RegistrationUtils.removeSui(mockContext);

        when(mockJson.get(SCHEMA)).thenReturn(true);
        when(mockParams.getScheme()).thenReturn(null);

        verifyStatic();
        RegistrationUtils.removeSui(mockContext);
    }

}
