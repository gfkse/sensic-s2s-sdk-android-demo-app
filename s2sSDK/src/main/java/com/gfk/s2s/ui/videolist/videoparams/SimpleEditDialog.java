package com.gfk.s2s.ui.videolist.videoparams;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gfk.s2s.demo.s2s.R;

public class SimpleEditDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_key_value_edit,container,false);
        final EditText editKey = rootView.findViewById(R.id.dialog_simple_edit_key);
        final EditText editValue = rootView.findViewById(R.id.dialog_simple_edit_value);
        Button buttonOk = rootView.findViewById(R.id.buttonOK);
        buttonOk.setOnClickListener(view -> finishDialog(editKey.getText().toString(),editValue.getText().toString()));
        Button buttonCancel = rootView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(view -> finishDialog("",""));

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_Alert);
        }

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Neuer Parameter");
        return dialog;

    }

    private void finishDialog(String key,String value){
        if(!key.isEmpty() && !value.isEmpty()) {
            Intent result = new Intent();
            result.putExtra(VideoParamsDialog.PARAM_KEY, key);
            result.putExtra(VideoParamsDialog.PARAM_VALUE, value);
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(VideoParamsDialog.NEW_PARAM_REQUEST, Activity.RESULT_OK, result);
            }
        }

        dismiss();
    }
}
