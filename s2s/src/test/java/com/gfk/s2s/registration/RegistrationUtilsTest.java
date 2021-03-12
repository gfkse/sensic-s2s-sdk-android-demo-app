package com.gfk.s2s.registration;

import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.gfk.s2s.registration.RegistrationUtils.changeJsonObject;
import static com.gfk.s2s.registration.RegistrationUtils.getJsonObject;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class RegistrationUtilsTest {

    Uri uri = Uri.parse("sensic1://sensic.net/?pid=11&paneluserid=123456&goto=https://www.google.de/?id=3243&webview=https://gfk.com/?id=12&suigenerator=https://gfk.com/?id=12&state={\"sensic1\":\"ShouldConnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}&processstateserver=https://sensic.net/stateserver&processid=bfao12faq2389&lastapp=sensic1&suitp=https://gfk.com/?id=12");

    @Test
    public void getWebViewUrlWorks() {
        String expected = "https://gfk.com/?id=12&pid=11&lastapp=sensic1&paneluserid=123456&webview=https://gfk.com/?id=12&state=%7B%22sensic1%22:%22ShouldConnect%22,%22sensic2%22:%22ShouldConnect%22,%22sensic3%22:%22ShouldConnect%22%7D&processstateserver=https://sensic.net/stateserver&processid=bfao12faq2389&suigenerator=https://gfk.com/?id=12&goto=https://www.google.de/?id=3243&suitp=https://gfk.com/?id=12";
        RegistrationQueryParams registrationQueryParams = new RegistrationQueryParams(uri);
        assertEquals(expected, registrationQueryParams.getWebViewUrl());
    }

    @Test
    public void getEnrichedUrlWorks() {
        String expected = "https://gfk.com/?id=12&ai=&pid=11&paneluserid=123456&f=json";
        RegistrationQueryParams registrationQueryParams = new RegistrationQueryParams(uri);
        assertEquals(expected, registrationQueryParams.getEnrichedSuiGeneratorUrl(ApplicationProvider.getApplicationContext()));
    }

    @Test
    public void changeJsonObjectWorks() throws JSONException {
        String deepLink = "{\"sensic1\":\"ShouldConnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}";
        JSONObject stateAsJsonObject = getJsonObject(deepLink);
        changeJsonObject(stateAsJsonObject, "sensic1");
        assertEquals(AppState.CONNECTED.toString(), stateAsJsonObject.get("sensic1"));

        deepLink = "{\"sensic1\":\"ShouldDisconnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}";
        stateAsJsonObject = getJsonObject(deepLink);
        changeJsonObject(stateAsJsonObject, "sensic1");
        assertEquals(AppState.DISCONNECTED.toString(), stateAsJsonObject.get("sensic1"));
    }
}