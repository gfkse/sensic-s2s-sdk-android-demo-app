package com.gfk.s2s.registration;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@PrepareForTest(RegistrationUtils.class)
@RunWith(PowerMockRunner.class)
public class RegistrationQueryParamsUnitTest {

    @Test
    public void registrationQueryParamsInitWorks() {
        Uri uriMock = mock(Uri.class);
        when(uriMock.getScheme()).thenReturn("demo");
        when(uriMock.getQueryParameter("pid")).thenReturn("value");
        when(uriMock.getQueryParameter("paneluserid")).thenReturn("value2");
        when(uriMock.getQueryParameter("webview")).thenReturn("value3");
        when(uriMock.getQueryParameter("goto")).thenReturn("value4");
        when(uriMock.getQueryParameter("suigenerator")).thenReturn("value5");
        when(uriMock.getQueryParameter("state")).thenReturn("value6");
        when(uriMock.getQueryParameter("processstateserver")).thenReturn("value8");
        when(uriMock.getQueryParameter("processid")).thenReturn("value9");
        when(uriMock.getQueryParameter("lastapp")).thenReturn("value10");
        when(uriMock.getQueryParameter("suitp")).thenReturn("value11");
        RegistrationQueryParams registrationQueryParams = new RegistrationQueryParams(uriMock);
        assertEquals(registrationQueryParams.getPanelId(), "value");
        assertEquals(registrationQueryParams.getPanelUserId(), "value2");
        assertEquals(registrationQueryParams.getTouchPointUrl(), "value3");
        assertEquals(registrationQueryParams.getGotoUrl(), "value4");
        assertEquals(registrationQueryParams.getState(), "value6");
        assertEquals(registrationQueryParams.getSuiGeneratorUrl(), "value5");
        assertEquals(registrationQueryParams.getScheme(), "demo");
        assertEquals(registrationQueryParams.getProcessStateServerUrl(), "value8");
        assertEquals(registrationQueryParams.getProcessId(), "value9");
        assertEquals(registrationQueryParams.getLastApp(), "value10");
        assertEquals(registrationQueryParams.getSuiTpUrl(), "value11");
        assertEquals(registrationQueryParams.getNotifyUrl(), "value8/value9/notify");
    }

    @Test
    public void registrationQueryParamsInitWithNullWorks() {
        Uri uriMock = mock(Uri.class);
        when(uriMock.getScheme()).thenReturn("demo");
        when(uriMock.getQueryParameter("pid")).thenReturn(null);
        when(uriMock.getQueryParameter("paneluserid")).thenReturn(null);
        when(uriMock.getQueryParameter("webview")).thenReturn(null);
        when(uriMock.getQueryParameter("goto")).thenReturn(null);
        when(uriMock.getQueryParameter("suigeneratorurl")).thenReturn(null);
        when(uriMock.getQueryParameter("state")).thenReturn(null);
        when(uriMock.getQueryParameter("processstateserver")).thenReturn(null);
        when(uriMock.getQueryParameter("processid")).thenReturn(null);
        when(uriMock.getQueryParameter("lastapp")).thenReturn(null);
        when(uriMock.getQueryParameter("suitp")).thenReturn(null);
        RegistrationQueryParams registrationQueryParams = new RegistrationQueryParams(uriMock);
        assertEquals(registrationQueryParams.getPanelId(), "");
        assertEquals(registrationQueryParams.getPanelUserId(), "");
        assertEquals(registrationQueryParams.getTouchPointUrl(), "");
        assertEquals(registrationQueryParams.getGotoUrl(), "");
        assertEquals(registrationQueryParams.getState(), "");
        assertEquals(registrationQueryParams.getSuiGeneratorUrl(), "");
        assertEquals(registrationQueryParams.getScheme(), "demo");
        assertEquals(registrationQueryParams.getProcessStateServerUrl(), "");
        assertEquals(registrationQueryParams.getProcessId(), "");
        assertEquals(registrationQueryParams.getLastApp(), "");
        assertEquals(registrationQueryParams.getSuiTpUrl(), "");
    }

    @Test
    public void appendQueryWorks() throws URISyntaxException {
        //Encoding works
        String expected = "https://gfk.com/?id=12&%7B%22sensi%3Ec1%22:%22ShouldC%3Eonnect%22,%22sensic2%22:%22ShouldConnect%22,%22sensic3%22:%22ShouldConnect%22%7D";
        assertEquals(expected, RegistrationQueryParams.appendUri("https://gfk.com/?id=12", "{\"sensi>c1\":\"ShouldC>onnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}").toString());

        expected = "https://gfk.com?%7B%22sensi%3Ec1%22:%22ShouldC%3Eonnect%22,%22sensic2%22:%22ShouldConnect%22,%22sensic3%22:%22ShouldConnect%22%7D";
        assertEquals(expected, RegistrationQueryParams.appendUri("https://gfk.com", "{\"sensi>c1\":\"ShouldC>onnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}").toString());
    }
}