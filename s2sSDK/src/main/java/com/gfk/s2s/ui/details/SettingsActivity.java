package com.gfk.s2s.ui.details;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.Endpoint;
import com.gfk.s2s.demo.s2s.R;

public class SettingsActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");
        final Switch optinSwitch = findViewById(R.id.optinSwitch);
        final Switch demoSwitch = findViewById(R.id.demoSwitch);
        final Switch preProdSwitch = findViewById(R.id.preProdSwitch);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        optinSwitch.setChecked(preferences.getBoolean("optin", true));

        Endpoint endpoint = Endpoint.fromId(preferences.getInt("endpoint", 1));
        switch (endpoint) {
            case DEMO:
                demoSwitch.setChecked(true);
                preProdSwitch.setChecked(false);
                break;
            case PREPROD:
                preProdSwitch.setChecked(true);
                demoSwitch.setChecked(false);
                break;
        }

        optinSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("optin", isChecked);
            editor.apply();
        });

        demoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editor.putInt("endpoint", Endpoint.DEMO.getId());
                preProdSwitch.setChecked(false);
            } else {
                editor.putInt("endpoint", Endpoint.PREPROD.getId());
                preProdSwitch.setChecked(true);
            }
            editor.apply();
        });

        preProdSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editor.putInt("endpoint", Endpoint.PREPROD.getId());
                demoSwitch.setChecked(false);
            } else {
                editor.putInt("endpoint", Endpoint.DEMO.getId());
                demoSwitch.setChecked(true);
            }
            editor.apply();
        });
    }
}
