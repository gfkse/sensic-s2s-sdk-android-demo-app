package com.gfk.s2s.collector.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import static android.content.Context.UI_MODE_SERVICE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
import static android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;
import static android.content.res.Configuration.UI_MODE_TYPE_WATCH;

public class DeviceInfo {

    private static final String UNKNOWN = "UNKNOWN";

    private DeviceInfo() {
        throw new IllegalStateException("Utility class");
    }

    public static String getDeviceType(Context context) {
        if (context == null) return UNKNOWN;
        if (hasMirroredDisplays(context)) {
            return "TV-MIRRORING";
        }
        if (isFireTvDevice(context)) {
            return "FIRETV";
        }
        if (isTvDevice(context)) {
            return "TV";
        }
        if (isWatchDevice(context)) {
            return UNKNOWN;
        }
        if (hasLargeScreen(context)) {
            return "TABLET";
        }
        if (hasMobileRadioCommunication(context)) {
            return "SMARTPHONE";
        }
        return UNKNOWN;
    }

    public static boolean isFireTvDevice(Context context) {
        if (context == null)
            return false;
        if (Build.MODEL.matches("AFTN")) {
            return true;
        } else
            return context.getPackageManager().hasSystemFeature("amazon.hardware.fire_tv");
    }

    private static boolean isTvDevice(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null
               && uiModeManager.getCurrentModeType() == UI_MODE_TYPE_TELEVISION;
    }

    private static boolean isWatchDevice(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
            return uiModeManager != null
                    && uiModeManager.getCurrentModeType() == UI_MODE_TYPE_WATCH;
        }
        return false;
    }

    private static boolean hasLargeScreen(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return (configuration.screenLayout & SCREENLAYOUT_SIZE_MASK)
                >= SCREENLAYOUT_SIZE_LARGE;
    }

    private static boolean hasMobileRadioCommunication(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager != null
            && manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }


    private static boolean hasMirroredDisplays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return checkIfDeviceIsMirroring(context);
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean checkIfDeviceIsMirroring(Context context) {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            return displayManager.getDisplays().length > 1;
        }
        return false;
    }
}
