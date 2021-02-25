package com.gfk.s2s.registration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.gfk.s2s.utils.android.factory.IntentFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({RegistrationUtils.class, Uri.class, IntentFactory.class})
@RunWith(PowerMockRunner.class)
public class RegistrationWebViewClientUnitTest {

    private static final String FINAL_GO_TO_URL = "sensic://go.to.com";
    private static final String APP_GO_TO_URL = "sensic1://go.to.com";
    private static final String HTTPS_URL = "https://some.url.com";
    private static final String HTTP_URL = "http://some.url.com";

    @Mock
    Context mockContext;

    @Mock
    Context webViewContext;

    @Mock
    ViewGroup mockView;

    @Mock
    WebView mockWebView;

    @Mock
    Intent mockIntent;

    RegistrationWebViewClient client;

    @Before
    public void before() {
        client = new RegistrationWebViewClient(mockContext, mockView);
    }

    @Test
    public void onPageFinished() {
        mockStatic(RegistrationUtils.class);

        when(mockContext.getApplicationContext()).thenReturn(webViewContext);

        client.onPageFinished(mockWebView, HTTP_URL);

        verifyStatic();
        RegistrationUtils.setAppIconInsideWebView(mockWebView, webViewContext);
    }

    @Test
    public void shouldOverrideUrlLoadingUrlStartsWithHttps() {
        assertTrue(client.shouldOverrideUrlLoading(mockWebView, HTTPS_URL));
        verify(mockWebView).loadUrl(HTTPS_URL);
    }

    @Test
    public void shouldOverrideUrlLoadingUrlStartsWithHttp() {
        assertTrue(client.shouldOverrideUrlLoading(mockWebView, HTTP_URL));
        verify(mockWebView).loadUrl(HTTP_URL);
    }

    @Test
    public void shouldOverrideUrlLoadingUrlStartsWithOutHttps() {
        mockStatic(Uri.class);
        mockStatic(IntentFactory.class);
        Uri uriMock = mock(Uri.class);
        when(Uri.parse(FINAL_GO_TO_URL)).thenReturn(uriMock);

        given(mockWebView.getContext()).willReturn(webViewContext);
        PackageManager packageManager = mock(PackageManager.class);
        ComponentName componentName = mock(ComponentName.class);

        when(mockContext.getPackageManager()).thenReturn(packageManager);
        when(mockIntent.resolveActivity(packageManager)).thenReturn(componentName);
        given(IntentFactory.build(Intent.ACTION_VIEW, uriMock)).willReturn(mockIntent);

        assertTrue(client.shouldOverrideUrlLoading(mockWebView, FINAL_GO_TO_URL));

        verify(mockView).removeView(mockWebView);
        verify(mockContext).startActivity(mockIntent);
        verify(mockWebView, never()).loadUrl(FINAL_GO_TO_URL);
    }


    @Test
    public void shouldOverrideUrlLoadingNoHttpAndNoFinalUrlButContextHasPackageManager() {
        mockStatic(Uri.class);
        mockStatic(RegistrationUtils.class);
        mockStatic(IntentFactory.class);

        Uri uriMock = mock(Uri.class);
        when(Uri.parse(APP_GO_TO_URL)).thenReturn(uriMock);

        PackageManager packageManager = mock(PackageManager.class);
        ComponentName componentName = mock(ComponentName.class);

        when(mockContext.getPackageManager()).thenReturn(packageManager);
        when(mockIntent.resolveActivity(packageManager)).thenReturn(componentName);
        when(IntentFactory.build(Intent.ACTION_VIEW, uriMock)).thenReturn(mockIntent);

        assertTrue(client.shouldOverrideUrlLoading(mockWebView, APP_GO_TO_URL));

        verify(mockView).removeView(mockWebView);
        verify(mockContext).startActivity(mockIntent);
        verify(mockWebView, never()).loadUrl(APP_GO_TO_URL);
        verify(mockView, never()).getContext();

        verifyStatic();
        RegistrationUtils.runJavascript("window.cancelProcess();", mockWebView);
        verifyStatic();
        Uri.parse(APP_GO_TO_URL);

    }

    @Test
    public void shouldOverrideUrlLoadingNoHttpAndNoFinalUrlContextHasNoPackageManager() {
        mockStatic(Uri.class);
        mockStatic(RegistrationUtils.class);
        mockStatic(IntentFactory.class);

        Uri uriMock = mock(Uri.class);
        when(Uri.parse(APP_GO_TO_URL)).thenReturn(uriMock);

        PackageManager packageManager = mock(PackageManager.class);

        when(mockContext.getPackageManager()).thenReturn(packageManager);
        when(mockIntent.resolveActivity(packageManager)).thenReturn(null);

        given(IntentFactory.build(Intent.ACTION_VIEW, uriMock)).willReturn(mockIntent);

        assertTrue(client.shouldOverrideUrlLoading(mockWebView, APP_GO_TO_URL));

        verify(mockView, never()).getContext();
        verify(mockView, never()).removeView(mockWebView);
        verify(mockContext, never()).startActivity(mockIntent);
        verify(mockWebView, never()).loadUrl(APP_GO_TO_URL);

        verifyStatic();
        Uri.parse(APP_GO_TO_URL);
    }
}
