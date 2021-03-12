package com.gfk.s2s.connect;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gfk.s2s.R;
import com.gfk.s2s.registration.S2SRegistration;

public class ConnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Uri data = getIntent().getData();
        // Only proceed if the app was opened with the VIEW action.
        if (S2SRegistration.isSensicUrl(data)) {
            S2SRegistration.storePanelist(findViewById(R.id.connectView), data);
        }
    }
}
