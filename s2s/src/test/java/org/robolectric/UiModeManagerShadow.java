package org.robolectric;

import android.app.UiModeManager;
import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.IOException;

@Implements(UiModeManager.class)
public class UiModeManagerShadow {

    private int currentModeType = 0;

    @Implementation
    public int getCurrentModeType() {
        return currentModeType;
    }

    public void setCurrentModeType(int type) {
        currentModeType = type;
    }
}
