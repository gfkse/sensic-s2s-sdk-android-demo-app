
package com.gfk.s2s.collector.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.LibTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.UiModeManagerShadow;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBuild;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowTelephonyManager;

import static android.content.Context.UI_MODE_SERVICE;
import static android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;
import static android.content.res.Configuration.UI_MODE_TYPE_WATCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(LibTestRunner.class)
public class DeviceInfoTest {
    private Context context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void getDeviceType_whenDeviceHasGsm_returnsSmartphone() {
        ShadowTelephonyManager telephonyManager = Shadows.shadowOf((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE));
        telephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_GSM);
        assertEquals("SMARTPHONE", DeviceInfo.getDeviceType(context));
    }

    @Test
    @Config(qualifiers = "+w600dp-h1024dp")
    public void getDeviceType_whenScreenIsLarge_returnsTablet() {
        assertEquals("TABLET", DeviceInfo.getDeviceType(context));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP, shadows = { UiModeManagerShadow.class })
    public void getDeviceType_whenUiModeIsWatch_returnsUnknown() {
        UiModeManagerShadow uiModeManager = Shadow.extract(context.getSystemService(UI_MODE_SERVICE));
        uiModeManager.setCurrentModeType(UI_MODE_TYPE_WATCH);
        assertEquals("UNKNOWN", DeviceInfo.getDeviceType(context));
    }

    @Test
    @Config(shadows = { UiModeManagerShadow.class })
    public void getDeviceType_whenUiModeIsTV_returnsTv() {
        UiModeManagerShadow uiModeManager = Shadow.extract(context.getSystemService(UI_MODE_SERVICE));
        uiModeManager.setCurrentModeType(UI_MODE_TYPE_TELEVISION);
        assertEquals("TV", DeviceInfo.getDeviceType(context));
    }

    @Test
    public void getDeviceType_whenDeviceHasFireTvSystemFeature_returnsFiretv() {
        ShadowPackageManager packageManager = Shadows.shadowOf(context.getPackageManager());
        packageManager.setSystemFeature("amazon.hardware.fire_tv", true);
        assertEquals("FIRETV", DeviceInfo.getDeviceType(context));
    }

    @Test
    public void getDeviceType_whenDeviceHasAnotherDisplay_returnsTVMirroring() {
        ShadowDisplayManager.addDisplay("");
        assertEquals("TV-MIRRORING", DeviceInfo.getDeviceType(context));
    }

    @Test
    public void isFireTvDevice_ifModelIsAFTN_shouldReturnTrue() {
        ShadowBuild.setModel("AFTN");
        assertTrue(DeviceInfo.isFireTvDevice(context));
    }

    @Test
    public void getDeviceType_whenDeviceIsNotAPhone_returnsUnknown() {
        ShadowTelephonyManager telephonyManager = Shadows.shadowOf((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE));
        telephonyManager.setPhoneType(TelephonyManager.PHONE_TYPE_NONE);
        assertEquals("UNKNOWN", DeviceInfo.getDeviceType(context));
    }

}

