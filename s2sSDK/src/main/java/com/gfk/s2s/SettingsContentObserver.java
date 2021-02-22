package com.gfk.s2s;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

public class SettingsContentObserver extends ContentObserver {

    private int previousVolume;
    private Context context;

    protected SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context = c;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = 0;
        if (audio != null) {
            currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        int delta = previousVolume - currentVolume;

        if(delta > 0) {
            Log.d("volume","Decreased" + currentVolume);
            previousVolume = currentVolume;
            quieter(currentVolume);
        }
        else if(delta < 0) {
            Log.d("volume","Increased" + currentVolume);
            previousVolume = currentVolume;
            louder(currentVolume);
        }
    }

    public void louder(int currentVolume) {

    }

    public void quieter(int currentVolume) {

    }
}
