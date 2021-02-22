package com.gfk.s2s.ui.videolist.offset;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gfk.s2s.demo.s2s.R;

public class OffsetDialog extends DialogFragment {

    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_offset,container,false);
        final EditText editValue = rootView.findViewById(R.id.dialog_offset_value);

        Context context = getActivity().getApplicationContext();
        if (context != null) {
            preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            int offset = preferences.getInt("offset", 0);
            editValue.setText(offset, TextView.BufferType.EDITABLE);
        }

        Button buttonOk = rootView.findViewById(R.id.buttonOK);
        buttonOk.setOnClickListener(view -> finishDialog(editValue.getText().toString()));
        Button buttonCancel = rootView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(view -> finishDialog(""));

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
        }
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Offset bearbeiten");

        return dialog;
    }

    private void finishDialog(String value){
        if(!value.isEmpty()) {
            int intValue = 0;

            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                Log.e("GfKlog", nfe.getMessage());
            }

            if (preferences != null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("offset", intValue);
                editor.apply();
            }
        }
        dismiss();
    }
}