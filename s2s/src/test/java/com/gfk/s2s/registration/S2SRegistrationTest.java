package com.gfk.s2s.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.gfk.s2s.collector.Collector.SHARED_PREFS_FILE;
import static com.gfk.s2s.collector.Collector.SUI_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class S2SRegistrationTest {

    @Test
    public void isSensicUrlWorks() {
        assertFalse(S2SRegistration.isSensicUrl(Uri.parse("sensic1://fo.net/akjdfkjsahfd/kajshdfjkhd/oakjdlkdj")));
        assertFalse(S2SRegistration.isSensicUrl(null));
        assertTrue(S2SRegistration.isSensicUrl(Uri.parse("seNsic1://sensic.net/akjdfkjsahfd/kajshdfjkhd/oakjdlkdj")));
        assertTrue(S2SRegistration.isSensicUrl(Uri.parse("sensic1://sensic.net/akjdfkjsahfd/kajshdfjkhd/oakjdlkdj")));
    }

    @Test
    public void storePanelistWorks() {
        Context context = ApplicationProvider.getApplicationContext();
        LinearLayout linearLayout = new LinearLayout(context);
        View parent = new View(context);
        linearLayout.addView(parent);
        S2SRegistration.storePanelist((ViewGroup) parent.getParent(), Uri.parse("sensic1://sensic.net/?pid=11&paneluserid=123456&webview=https://interappcommunication-poc.s3.eu-central-1.amazonaws.com/app-touchpoint-auto.html&suigenerator=https://demo-config-preproduction.sensic.net/suigenerator&goto=https://www.google.de&state={\"sensic1\":\"ShouldConnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}&notifyurl=https://www.server.de/123/notify"));
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        assertTrue(prefs.contains(SUI_ID));
        S2SRegistration.storePanelist((ViewGroup) parent.getParent(), Uri.parse("sensic1://sensic.net/?pid=11&paneluserid=123456&webview=https://interappcommunication-poc.s3.eu-central-1.amazonaws.com/app-touchpoint-auto.html&suigenerator=https://demo-config-preproduction.sensic.net/suigenerator&goto=https://www.google.de&state={\"sensic1\":\"ShouldDisconnect\",\"sensic2\":\"ShouldConnect\",\"sensic3\":\"ShouldConnect\"}&notifyurl=https://www.server.de/123/notify"));
        assertFalse(prefs.contains(SUI_ID));
    }
}